public class Venta {

    private Integer idProducto;
    private Integer cantidadProductoVendido;

    public Venta(Integer idProducto, Integer cantidadProductoVendido) {
        this.idProducto = idProducto;
        this.cantidadProductoVendido = cantidadProductoVendido;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadProductoVendido() {
        return cantidadProductoVendido;
    }

    public void setCantidadProductoVendido(Integer cantidadProductoVendido) {
        this.cantidadProductoVendido = cantidadProductoVendido;
    }
}