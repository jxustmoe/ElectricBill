package cn.jxust.electricBill.pojo;

/**
 * 返回给用户的信息
 */
public class Message {

    //错误码
    private int code;

    //返回的数据
    private Object data;

    public Message(int code, Object data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
