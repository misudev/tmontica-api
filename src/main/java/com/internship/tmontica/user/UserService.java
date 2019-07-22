package com.internship.tmontica.user;

import com.internship.tmontica.repository.UserDao;
import com.internship.tmontica.security.JwtInterceptor;
import com.internship.tmontica.security.JwtService;
import com.internship.tmontica.user.exception.*;
import com.internship.tmontica.user.model.request.*;
import com.internship.tmontica.user.model.response.UserInfoRespDTO;
import com.internship.tmontica.security.AuthenticationKey;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class UserService { //implements UserDetail

    private final UserDao userDao;
    //private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final MailSender sender;
    private final JwtService jwtService;
    private static final String SESSION_USER_ID = "userId";

    private enum MailOption {
        findId, findPassword
    }

    @Transactional
    public boolean signUp(UserSignUpReqDTO userSignUpReqDTO){
        //userSignUpReqDTO.setPassword(passwordEncoder.encode(user.getPassword())); need passwordEnconding
        checkUserIdDuplicatedException(userSignUpReqDTO.getId());
        setRole(userSignUpReqDTO);
        User user = modelMapper.map(userSignUpReqDTO, User.class);
        return userDao.addUser(user) > 0;
    }

    private void setRole(UserSignUpReqDTO userSignUpReqDTO){
        String role = userSignUpReqDTO.getRole();
        if(role == null || role.equals(UserRole.USER.toString())){
            userSignUpReqDTO.setRole("USER");
        } else if (role.equals(UserRole.ADMIN.toString())){
            userSignUpReqDTO.setRole("ADMIN");
        } else {
            throw new InvalidUserRoleException();
        }
    }

    @Transactional(readOnly = true)
    public boolean idDuplicateCheck(String id){
        return !isDuplicate(id);
    }

    private boolean isDuplicate(String id){
        User user = userDao.getUserByUserId(id);

        return !(user == null);
    }

    @Transactional(readOnly = true)
    public void signIn(UserSignInReqDTO userSignInReqDTO, HttpSession session) {

        checkUserIdNotFoundException(userSignInReqDTO.getId());
        checkPasswordMismatchException(userSignInReqDTO.getPassword(),
                userDao.getUserByUserId(userSignInReqDTO.getId()).getPassword());
        session.setAttribute(SESSION_USER_ID, userSignInReqDTO.getId());
    }

     public void makeJwtToken(UserSignInReqDTO userSignInReqDTO, HttpServletResponse response){
        response.setHeader(JwtInterceptor.HEADER_AUTH,
                jwtService.getToken(makeTokenUserWithRole(userSignInReqDTO.getId(),
                        userSignInReqDTO.getRole())));
    }

    private User makeTokenUserWithRole(String id, String role){

        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Transactional(readOnly = true)
    public void checkPassword(UserCheckPasswordReqDTO userCheckPasswordReqDTO, HttpSession session){

        String userId = session.getAttribute(SESSION_USER_ID).toString();
        checkMissingSessionUserIdException(userId);
        checkPasswordMismatchException(userCheckPasswordReqDTO.getPassword(),
                userDao.getUserByUserId(userId).getPassword());
    }

    private void checkMissingSessionUserIdException(String userId){

        if(userId == null){
            throw new MissingSessionUserIdException();
        }
    }

    @Transactional(readOnly = true)
    public UserInfoRespDTO getUserInfo(String id){

        checkUserIdNotFoundException(id);
        return modelMapper.map(userDao.getUserByUserId(id), UserInfoRespDTO.class);
    }

    // newPassword / newPasswordCheck / id세션처리
    @Transactional
    public boolean changePassword(UserChangePasswordReqDTO userChangePasswordReqDTO){

        checkUserIdNotFoundException(userChangePasswordReqDTO.getId());
        checkPasswordMismatchException(userChangePasswordReqDTO.getPassword(), userChangePasswordReqDTO.getNewPassword());
        User user = modelMapper.map(userChangePasswordReqDTO, User.class);
        return userDao.updateUserPassword(user) > 0;
    }

    @Transactional
    public boolean withDrawUser(String id){

        checkUserIdNotFoundException(id);
        return userDao.deleteUser(id) > 0;
    }


    @Transactional(readOnly = true)
    public boolean sendUserId(String email, HttpSession httpSession) throws MailSendException{
        User user = userDao.getUserByEmail(email);
        checkUserIdNotFoundException(user.getId());
        AuthenticationKey authenticationKey = new AuthenticationKey();
        sender.send(sendMail(user, MailOption.findId, authenticationKey.getAuthenticationKey()));
        httpSession.setAttribute("key", authenticationKey);
        return true;
    }

    // 비밀번호 어떤식으로 줄지?
    @Transactional
    public boolean sendUserPassword(String id, String email) throws MailSendException{
        User user = userDao.getUserByUserId(id);
        checkEmailMismatchException(user.getEmail(), email);
        sender.send(sendMail(user, MailOption.findPassword));
        return true;
    }

    private SimpleMailMessage sendMail(User user, MailOption mailOption, String... strings) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("tmontica701@gmail.com");
        msg.setTo(user.getEmail());

        switch(mailOption){
            case findId:
                msg.setSubject("[TMONG CAFFE]"+user.getName()+"님 아이디 찾기 메일입니다.");
                msg.setText("인증키는 "+strings[0]+"입니다. 본인과 관련없는 메일이라면 무시하시면 됩니다.");
                break;
            case findPassword:
                msg.setSubject("[TMONG CAFFE]"+user.getName()+"님 비밀번호 찾기 메일입니다.");
                msg.setText("찾으시는 비밀번호는 "+user.getPassword()+"입니다. 본인과 관련없는 메일이라면 무시하시면 됩니다.");
                break;
        }

        return msg;
    }

    private void checkUserIdDuplicatedException(String id){

        if(isDuplicate(id)){
            throw new UserIdDuplicatedException();
        }
    }

    private boolean isSamePassword(String password, String comparePassword){

        return password.equals(comparePassword);
    }

    private void checkPasswordMismatchException(String password, String comparePassword){

        if(!isSamePassword(password, comparePassword)){
            throw new PasswordMismatchException();
        }
    }

    private boolean isExistUser(String id){

        return userDao.getUserByUserId(id)!=null;
    }

    private void checkUserIdNotFoundException(String id){

        if(!isExistUser(id)){
            throw new UserIdNotFoundException();
        }
    }

    private boolean isCorrectEmail(String email, String compareEmail){

        return email.equals(compareEmail);
    }

    private void checkEmailMismatchException(String email, String compareEmail){

        if(!isCorrectEmail(email, compareEmail)){
            throw new EmailMismatchException();
        }
    }
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//
//        User user = userDao.getUserByUserId(userId);
//        List<GrantedAuthority> authorities = new ArrayList<>();
//               authorities.add(new SimpleGrantedAuthority("user"));
//        return new org.springframework.security.core.userdetails.User(user.getId(), user.getPassword(),authorities);
//
//    }

}