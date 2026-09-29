import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera los reportes de ventas a partir de los archivos de entrada.
 *
 * <p>Entradas (carpeta {@code input/}): {@code productos.txt},
 * {@code vendedores.txt} y un archivo de ventas por vendedor.
 *
 * <p>Salidas (carpeta {@code output/}): {@code reporte_vendedores.csv}
 * (vendedor;dinero recaudado, de mayor a menor) y
 * {@code reporte_productos.csv} (nombre;precio;cantidad vendida,
 * de mayor a menor cantidad).
 *
 * <p>Las lineas con formato erroneo o informacion incoherente (producto
 * inexistente, cantidad no positiva) se omiten con un aviso en consola
 * y no interrumpen el proceso.
 */
public class Main {

    /** Ruta del archivo maestro de productos. */
    private static final Path RUTA_PRODUCTOS = Paths.get("input", "productos.txt");

    /** Ruta del archivo maestro de vendedores. */
    private static final Path RUTA_VENDEDORES = Paths.get("input", "vendedores.txt");

    /** Carpeta con los archivos de entrada. */
    private static final Path CARPETA_ENTRADA = Paths.get("input");

    /** Carpeta donde se escriben los reportes. */
    private static final Path CARPETA_SALIDA = Paths.get("output");

    /** Reporte de vendedores ordenado por dinero recaudado. */
    private static final Path REPORTE_VENDEDORES =
            CARPETA_SALIDA.resolve("reporte_vendedores.csv");

    /** Reporte de productos ordenado por cantidad vendida. */
    private static final Path REPORTE_PRODUCTOS =
            CARPETA_SALIDA.resolve("reporte_productos.csv");

    /**
     * Punto de entrada del programa de reportes.
     *
     * <p>No solicita informacion al usuario. Muestra un mensaje de
     * finalizacion exitosa o un mensaje de error, segun el resultado.
     *
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        try {
            Map<Integer, Product> productos = leerProductos();
            Map<String, Salesman> vendedores = leerVendedores();

            Map<String, BigDecimal> recaudadoPorVendedor = inicializarRecaudado(vendedores);
            Map<Integer, Integer> cantidadPorProducto = inicializarCantidades(productos);

            procesarVentas(productos, vendedores, recaudadoPorVendedor, cantidadPorProducto);

            Files.createDirectories(CARPETA_SALIDA);
            escribirReporteVendedores(vendedores, recaudadoPorVendedor);
            escribirReporteProductos(productos, cantidadPorProducto);

            System.out.println("Reportes generados correctamente:");
            System.out.println("- " + REPORTE_VENDEDORES);
            System.out.println("- " + REPORTE_PRODUCTOS);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Error al generar los reportes: " + e.getMessage());
        }
    }

    /**
     * Lee el maestro de productos ({@code id;nombre;precio}).
     *
     * @return productos indexados por ID
     * @throws IOException si el archivo no existe o no se puede leer
     * @throws IllegalArgumentException si no hay productos validos
     */
    private static Map<Integer, Product> leerProductos() throws IOException {
        Map<Integer, Product> productos = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(RUTA_PRODUCTOS)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split(";");
                if (campos.length < 3) {
                    System.out.println("Advertencia: linea " + numeroLinea
                            + " de productos con formato erroneo, se omite.");
                    continue;
                }
                try {
                    Integer id = Integer.parseInt(campos[0].trim());
                    String nombre = campos[1].trim();
                    BigDecimal precio = new BigDecimal(campos[2].trim());
                    if (nombre.isEmpty() || precio.signum() < 0) {
                        System.out.println("Advertencia: linea " + numeroLinea
                                + " de productos con informacion incoherente, se omite.");
                        continue;
                    }
                    productos.put(id, new Product(id, nombre, precio));
                } catch (NumberFormatException e) {
                    System.out.println("Advertencia: linea " + numeroLinea
                            + " de productos con numero invalido, se omite.");
                }
            }
        }

        if (productos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay productos validos en input/productos.txt");
        }
        return productos;
    }

    /**
     * Lee el maestro de vendedores
     * ({@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}).
     *
     * @return vendedores indexados por numero de documento
     * @throws IOException si el archivo no existe o no se puede leer
     * @throws IllegalArgumentException si no hay vendedores validos
     */
    private static Map<String, Salesman> leerVendedores() throws IOException {
        Map<String, Salesman> vendedores = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(RUTA_VENDEDORES)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split(";");
                if (campos.length < 4
                        || campos[0].isBlank() || campos[1].isBlank()
                        || campos[2].isBlank() || campos[3].isBlank()) {
                    System.out.println("Advertencia: linea " + numeroLinea
                            + " de vendedores con formato erroneo, se omite.");
                    continue;
                }
                String numeroDocumento = campos[1].trim();
                vendedores.put(numeroDocumento, new Salesman(
                        campos[0].trim(),
                        numeroDocumento,
                        campos[2].trim(),
                        campos[3].trim()));
            }
        }

        if (vendedores.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay vendedores validos en input/vendedores.txt");
        }
        return vendedores;
    }

    /**
     * Crea el acumulado de dinero recaudado en cero para cada vendedor.
     *
     * @param vendedores vendedores conocidos
     * @return mapa numero de documento a dinero recaudado
     */
    private static Map<String, BigDecimal> inicializarRecaudado(
            Map<String, Salesman> vendedores) {
        Map<String, BigDecimal> recaudado = new HashMap<>();
        for (String documento : vendedores.keySet()) {
            recaudado.put(documento, BigDecimal.ZERO);
        }
        return recaudado;
    }

    /**
     * Crea el acumulado de cantidad vendida en cero para cada producto.
     *
     * @param productos productos conocidos
     * @return mapa ID de producto a cantidad vendida
     */
    private static Map<Integer, Integer> inicializarCantidades(
            Map<Integer, Product> productos) {
        Map<Integer, Integer> cantidades = new HashMap<>();
        for (Integer id : productos.keySet()) {
            cantidades.put(id, 0);
        }
        return cantidades;
    }

    /**
     * Recorre los archivos de ventas de {@code input/} y acumula el dinero
     * recaudado por vendedor y la cantidad vendida por producto.
     *
     * <p>Se usa solo el numero de documento de la cabecera para identificar
     * al vendedor, porque el tipo de documento de esa cabecera se genera
     * de forma aleatoria e independiente del maestro de vendedores.
     *
     * @param productos catalogo de productos
     * @param vendedores vendedores conocidos
     * @param recaudadoPorVendedor acumulado de dinero por documento (se modifica)
     * @param cantidadPorProducto acumulado de cantidad por ID (se modifica)
     * @throws IOException si la carpeta de entrada no se puede explorar
     */
    private static void procesarVentas(
            Map<Integer, Product> productos,
            Map<String, Salesman> vendedores,
            Map<String, BigDecimal> recaudadoPorVendedor,
            Map<Integer, Integer> cantidadPorProducto) throws IOException {
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(
                CARPETA_ENTRADA, "*.txt")) {
            for (Path archivo : archivos) {
                String nombre = archivo.getFileName().toString();
                if (nombre.equals("productos.txt") || nombre.equals("vendedores.txt")) {
                    continue;
                }
                procesarArchivoVentas(
                        archivo, productos, vendedores,
                        recaudadoPorVendedor, cantidadPorProducto);
            }
        }
    }

    /**
     * Procesa un archivo de ventas: cabecera {@code TipoDocumento;NumeroDocumento}
     * seguida de lineas {@code IDProducto;Cantidad;}.
     *
     * @param archivo ruta del archivo de ventas
     * @param productos catalogo de productos
     * @param vendedores vendedores conocidos
     * @param recaudadoPorVendedor acumulado de dinero por documento (se modifica)
     * @param cantidadPorProducto acumulado de cantidad por ID (se modifica)
     */
    private static void procesarArchivoVentas(
            Path archivo,
            Map<Integer, Product> productos,
            Map<String, Salesman> vendedores,
            Map<String, BigDecimal> recaudadoPorVendedor,
            Map<Integer, Integer> cantidadPorProducto) {
        try (BufferedReader reader = Files.newBufferedReader(archivo)) {
            String cabecera = reader.readLine();
            if (cabecera == null || cabecera.isBlank()) {
                System.out.println("Advertencia: " + archivo.getFileName()
                        + " sin cabecera, se omite.");
                return;
            }
            String[] datosCabecera = cabecera.split(";");
            if (datosCabecera.length < 2 || datosCabecera[1].isBlank()) {
                System.out.println("Advertencia: " + archivo.getFileName()
                        + " con cabecera erronea, se omite.");
                return;
            }
            String numeroDocumento = datosCabecera[1].trim();
            if (!vendedores.containsKey(numeroDocumento)) {
                System.out.println("Advertencia: " + archivo.getFileName()
                        + " de un vendedor desconocido, se omite.");
                return;
            }

            String linea;
            int numeroLinea = 1;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split(";");
                if (campos.length < 2) {
                    System.out.println("Advertencia: " + archivo.getFileName()
                            + " linea " + numeroLinea + " con formato erroneo, se omite.");
                    continue;
                }
                try {
                    Integer idProducto = Integer.parseInt(campos[0].trim());
                    int cantidad = Integer.parseInt(campos[1].trim());
                    Product producto = productos.get(idProducto);
                    if (producto == null) {
                        System.out.println("Advertencia: " + archivo.getFileName()
                                + " linea " + numeroLinea
                                + " con producto inexistente, se omite.");
                        continue;
                    }
                    if (cantidad <= 0) {
                        System.out.println("Advertencia: " + archivo.getFileName()
                                + " linea " + numeroLinea
                                + " con cantidad no positiva, se omite.");
                        continue;
                    }
                    BigDecimal valorVenta = producto.getPrecioPorUnidadProducto()
                            .multiply(BigDecimal.valueOf(cantidad));
                    recaudadoPorVendedor.merge(numeroDocumento, valorVenta, BigDecimal::add);
                    cantidadPorProducto.merge(idProducto, cantidad, Integer::sum);
                } catch (NumberFormatException e) {
                    System.out.println("Advertencia: " + archivo.getFileName()
                            + " linea " + numeroLinea + " con numero invalido, se omite.");
                }
            }
        } catch (IOException e) {
            System.out.println("Advertencia: no se pudo leer "
                    + archivo.getFileName() + ", se omite.");
        }
    }

    /**
     * Escribe el reporte de vendedores ({@code nombre;dineroRecaudado}),
     * ordenado de mayor a menor recaudacion.
     *
     * @param vendedores vendedores conocidos
     * @param recaudadoPorVendedor dinero acumulado por documento
     * @throws IOException si el reporte no se puede escribir
     */
    private static void escribirReporteVendedores(
            Map<String, Salesman> vendedores,
            Map<String, BigDecimal> recaudadoPorVendedor) throws IOException {
        List<Salesman> ordenados = new ArrayList<>(vendedores.values());
        ordenados.sort(Comparator
                .comparing((Salesman vendedor) -> recaudadoPorVendedor
                        .getOrDefault(vendedor.getNumeroDocumento(), BigDecimal.ZERO))
                .reversed()
                .thenComparing(vendedor -> nombreCompleto(vendedor)));

        try (BufferedWriter writer = Files.newBufferedWriter(REPORTE_VENDEDORES)) {
            for (Salesman vendedor : ordenados) {
                BigDecimal total = recaudadoPorVendedor
                        .getOrDefault(vendedor.getNumeroDocumento(), BigDecimal.ZERO);
                writer.write(nombreCompleto(vendedor) + ";" + total.toPlainString());
                writer.newLine();
            }
        }
    }

    /**
     * Escribe el reporte de productos ({@code nombre;precio;cantidadVendida}),
     * ordenado de mayor a menor cantidad vendida.
     *
     * @param productos catalogo de productos
     * @param cantidadPorProducto cantidad acumulada por ID
     * @throws IOException si el reporte no se puede escribir
     */
    private static void escribirReporteProductos(
            Map<Integer, Product> productos,
            Map<Integer, Integer> cantidadPorProducto) throws IOException {
        List<Product> ordenados = new ArrayList<>(productos.values());
        ordenados.sort(Comparator
                .comparing((Product producto) -> cantidadPorProducto
                        .getOrDefault(producto.getIdProducto(), 0))
                .reversed()
                .thenComparing(Product::getNombreProducto));

        try (BufferedWriter writer = Files.newBufferedWriter(REPORTE_PRODUCTOS)) {
            for (Product producto : ordenados) {
                int cantidad = cantidadPorProducto
                        .getOrDefault(producto.getIdProducto(), 0);
                writer.write(producto.getNombreProducto() + ";"
                        + producto.getPrecioPorUnidadProducto().toPlainString() + ";"
                        + cantidad);
                writer.newLine();
            }
        }
    }

    /**
     * Construye el nombre completo de un vendedor.
     *
     * @param vendedor vendedor
     * @return nombres y apellidos separados por espacio
     */
    private static String nombreCompleto(Salesman vendedor) {
        return vendedor.getNombresVendedor() + " " + vendedor.getApellidosVendedor();
    }
}
