import java.io.Serializable;

public class Packet implements Serializable {

  public int id;
  public String msg;

  public Packet(int id, String msg) {
    super();
    this.id = id;
    this.msg = msg;
  }

  public int getId() {
    return id;
  }

  public void setId(int setId) {
    this.id = setId;
  }

  public String getMsg() {
    return msg;
  }
}
