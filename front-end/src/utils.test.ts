import * as utils from "./utils";
import history from "./history";

window.alert = jest.fn();
const alert = jest.spyOn(window, "alert");
const push = jest.spyOn(history, "push");

describe("goToSignin", () => {
  it("메시지를 출력하고, signin으로 history 이동한다.", () => {
    const message = "로그인이 필요합니다.";
    utils.goToSignin(message);

    expect(alert).toHaveBeenCalledWith("로그인이 필요합니다.");
    expect(push).toHaveBeenCalledWith("/signin");
  });
});

