import java.math.BigDecimal;

public class Product {

    private Integer idProducto;
    private String nombreProducto;
    private BigDecimal precioPorUnidadProducto;

    public Product(Integer idProducto, String nombreProducto, BigDecimal precioPorUnidadProducto) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioPorUnidadProducto = precioPorUnidadProducto;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public BigDecimal getPrecioPorUnidadProducto() {
        return precioPorUnidadProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setPrecioPorUnidadProducto(BigDecimal precioPorUnidadProducto) {
        this.precioPorUnidadProducto = precioPorUnidadProducto;
    }
}
