import java.util.Random;

public class Salesman {

    // Datos base para la generacion pseudoaleatoria y coherente de vendedores
    private static final String[] TIPOS_DOCUMENTO = {"CC", "TI", "CE", "PA"};
    private static final String[] NOMBRES = {
            "Carlos", "María", "José", "Ana", "Luis", "Carmen", "Jorge", "Patricia",
            "Miguel", "Lucía", "Andrés", "Claudia", "Fernando", "Diana", "Ricardo", "Sara"
    };
    private static final String[] SEGUNDOS_NOMBRES = {
            "Antonio", "Fernanda", "Eduardo", "Camila", "Alejandro", "Valentina", "Gabriel", "Manuela",
            "Alberto", "Carolina", "Felipe", "Daniela", "Sebastián", "Natalia", "Ignacio", "Alejandra"
    };
    private static final String[] PRIMEROS_APELLIDOS = {
            "García", "Rodríguez", "Martínez", "López", "Hernández", "González", "Pérez", "Sánchez",
            "Ramírez", "Torres", "Flores", "Rivera", "Morales", "Ortiz", "Castañeda", "Vargas"
    };
    private static final String[] SEGUNDOS_APELLIDOS = {
            "Gómez", "Díaz", "Castro", "Romero", "Silva", "Mendoza", "Vega", "Rojas",
            "Aguilar", "Guzmán", "Salazar", "Peña", "Cordero", "Fuentes", "Navarro", "Gil"
    };

    // Unica instancia compartida de Random para toda la clase
    private static final Random RANDOM = new Random();

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombresVendedor;
    private String apellidosVendedor;

    //Metodo constructor de la clase
    public Salesman(String tipoDocumento, String numeroDocumento,
                    String nombresVendedor, String apellidosVendedor) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombresVendedor = nombresVendedor;
        this.apellidosVendedor = apellidosVendedor;
    }

    /*
     * Factoria: construye un vendedor pseudoaleatorio y coherente.
     * La coherencia se garantiza generando el numero de documento
     * segun el tipo de documento elegido.
     */
    public static Salesman aleatorio() {
        String tipoDocumento = TIPOS_DOCUMENTO[RANDOM.nextInt(TIPOS_DOCUMENTO.length)];
        String numeroDocumento = generarNumeroDocumento(tipoDocumento);
        String nombresVendedor = NOMBRES[RANDOM.nextInt(NOMBRES.length)] + " "
                + SEGUNDOS_NOMBRES[RANDOM.nextInt(SEGUNDOS_NOMBRES.length)];
        String apellidosVendedor = PRIMEROS_APELLIDOS[RANDOM.nextInt(PRIMEROS_APELLIDOS.length)] + " "
                + SEGUNDOS_APELLIDOS[RANDOM.nextInt(SEGUNDOS_APELLIDOS.length)];

        return new Salesman(tipoDocumento, numeroDocumento, nombresVendedor, apellidosVendedor);
    }

    /*
     * Devuelve un tipo de documento pseudoaleatorio, para construir
     * la cabecera de los archivos de ventas (createSalesMenFile).
     */
    public static String tipoDocumentoAleatorio() {
        return TIPOS_DOCUMENTO[RANDOM.nextInt(TIPOS_DOCUMENTO.length)];
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombresVendedor() {
        return nombresVendedor;
    }

    public void setNombresVendedor(String nombresVendedor) {
        this.nombresVendedor = nombresVendedor;
    }

    public String getApellidosVendedor() {
        return apellidosVendedor;
    }

    public void setApellidosVendedor(String apellidosVendedor) {
        this.apellidosVendedor = apellidosVendedor;
    }

    /*
     * Genera un numero de documento numerico coherente con el tipo:
     * TI -> 8 digitos, CE -> 7 digitos, PA -> 9 digitos, CC -> 10 digitos.
     * El primer digito nunca es cero, por lo que el numero siempre es valido.
     */
    private static String generarNumeroDocumento(String tipoDocumento) {
        int longitud = switch (tipoDocumento) {
            case "TI" -> 8;
            case "CE" -> 7;
            case "PA" -> 9;
            default -> 10; // CC
        };

        StringBuilder numero = new StringBuilder(longitud);
        numero.append(1 + RANDOM.nextInt(9));
        for (int i = 1; i < longitud; i++) {
            numero.append(RANDOM.nextInt(10));
        }
        return numero.toString();
    }
}