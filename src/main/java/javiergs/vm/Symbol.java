package javiergs.vm;

/**
 * Symbol class used to store the symbol type, scope and value
 *
 * @version 1.0
 * @author javiergs
 */
public class Symbol <t> {
  
  private String type;
  private String scope;
  private t value;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public t getValue() {
    return value;
  }

  public void setValue(t value) {
    this.value = value;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String Scope) {
    this.scope = Scope;
  }

  public Symbol(String type, String scope, t value) {
    this.type = type;
    this.scope = scope;
    this.value = value;
  }
  
}