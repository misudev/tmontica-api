import { handleError } from "../common";
import { CommonError } from "../CommonError";

window.alert = jest.fn();
const alert = jest.spyOn(window, "alert");

describe("handleError", () => {
  describe("CommonError 타입으로 에러가 들어온다.", () => {
    it("에러에 status 속성이 없으면 네트워크 오류 발생 경고를 띄운다.", () => {
      const error = new CommonError({});
      const result = handleError(error);

      expect(alert).toHaveBeenCalledWith("네트워크 오류 발생");
      result.then(promise => {
        expect(promise).toEqual(error);
      });
    });

    it("401에러가 발생하면 권한 경고를 띄우고, signout을 resolve 한다.", () => {
      const error = new CommonError({
        status: 401
      });
      const result = handleError(error);

      expect(alert).toHaveBeenCalledWith("권한이 필요한 요청입니다. 다시 로그인 해주세요.");
      result.then(promise => {
        expect(promise).toEqual("signout");
      });
    });

    it("에러 메시지에 JWT 문자가 포함되면, signout을 resolve 한다.", () => {
      const error = new CommonError({
        status: 500,
        message: "JWT"
      });
      const result = handleError(error);

      expect(alert).toHaveBeenCalledWith("권한이 필요한 요청입니다. 다시 로그인 해주세요.");
      result.then(promise => {
        expect(promise).toEqual("signout");
      });
    });
  });
});
