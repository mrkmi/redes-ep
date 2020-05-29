import java.io.Serializable;

public class Ack implements Serializable {
  
  public int id;
  
  public Ack(int id) {
    super();
    this.id = id;
  }
  
  public int getId() {
    return id;
  }
}
