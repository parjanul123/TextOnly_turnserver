// iban.java: Clasa pentru procesarea tranzacțiilor IBAN
package turnserver;

import java.io.Serializable;

public class IbanTransaction implements Serializable {
    private String ibanDest;
    private String deviceType;
    private boolean biometricsRequired;

    private String contBancar;
    // contul bancar sursă este gestionat de aplicație, nu de utilizator
    public IbanTransaction(String ibanDest, String deviceType, boolean biometricsRequired) {
        this.ibanDest = ibanDest;
        this.deviceType = deviceType;
        this.biometricsRequired = biometricsRequired;
    }
    // Setter pentru contul bancar
    public void setContBancar(String contBancar) {
        this.contBancar = contBancar;
    }
    // Getter pentru contul bancar (doar pentru uz intern)
    public String getContBancar() {
        return contBancar;
    }

    public String getIbanDest() { return ibanDest; }
    public String getDeviceType() { return deviceType; }
    public boolean isBiometricsRequired() { return biometricsRequired; }

    public String processTransaction() {
        if (biometricsRequired) {
            return "Tranzacție de pe mobil: autentificare biometrică necesară.";
        } else {
            return "Tranzacție de pe PC: continuă fără biometrie.";
        }
    }
}
