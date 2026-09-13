import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateInfoFiles {

    // Cantidades por defecto usadas en main()
    private static final int PRODUCTS_COUNT = 10;
    private static final int SALESMEN_COUNT = 10;
    private static final int SALES_PER_SALESMAN = 5;
    private static final int MAX_CANTIDAD_VENDIDA = 10;

    // Unica instancia compartida de Random para toda la clase
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        try {
            createProductsFile(PRODUCTS_COUNT);
            System.out.println("Archivo de productos generado correctamente.");

            createSalesManInfoFile(SALESMEN_COUNT);
            System.out.println("Archivo de vendedores generado correctamente.");

            for (Salesman salesman : leerVendedores()) {
                String archivoVentas = nombreArchivoVentas(salesman);
                createSalesMenFile(
                        SALES_PER_SALESMAN,
                        archivoVentas,
                        Long.parseLong(salesman.getNumeroDocumento())
                );
                System.out.println("Archivo de ventas generado: " + archivoVentas + ".txt");
            }

        } catch (IllegalArgumentException ext) {
            System.out.println(ext.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createProductsFile(int productsCount) throws IOException {

        //validaciones Guard
        if (productsCount <= 0) {
            throw new IllegalArgumentException(
              "La cantidad de productos debe ser mayor que cero"
            );
        }

        Random random = new Random();
        //Crea objeto path, construye y guarda la ruta.
        Path filePath = Paths.get("input", "productos.txt");

        //Crea la carpeta input si no existe
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {

            for (int i = 1; i <= productsCount; i++) {
                String name = "Producto " + i;
                BigDecimal price = BigDecimal.valueOf(
                        1000 + random.nextInt(999000)
                );

                Product product = new Product(i, name, price);

                writer.write(
                        product.getIdProducto() + ";" +
                                product.getNombreProducto() + ";" +
                                product.getPrecioPorUnidadProducto()
                );

                writer.newLine();
            }
        }
    }

    public static void createSalesManInfoFile(int salesmanCount) throws IOException {

        // Validacion guard
        if (salesmanCount <= 0) {
            throw new IllegalArgumentException(
              "La cantidad de vendedores debe ser mayor que cero"
            );
        }

        Path filePath = Paths.get("input", "vendedores.txt");

        // Crea la carpeta input si no existe
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {

            for (int i = 1; i <= salesmanCount; i++) {
                Salesman salesman = Salesman.aleatorio();

                writer.write(
                        salesman.getTipoDocumento() + ";" +
                                salesman.getNumeroDocumento() + ";" +
                                salesman.getNombresVendedor() + ";" +
                                salesman.getApellidosVendedor()
                );

                writer.newLine();
            }
        }
    }

    /*
     * Crea un archivo pseudoaleatorio de ventas para un vendedor.
     * Formato:
     *   TipoDocumentoVendedor;NumeroDocumentoVendedor
     *   IDProducto;CantidadProductoVendido;
     *   ...
     * El numero de lineas de venta lo determina randomSalesCount.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {

        // Validaciones guard
        if (randomSalesCount <= 0) {
            throw new IllegalArgumentException(
              "La cantidad de ventas debe ser mayor que cero"
            );
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
              "El nombre del vendedor no puede estar vacio"
            );
        }

        // Solo se venden productos que realmente existen en input/productos.txt
        List<Integer> idProductosValidos = leerIdProductosValidos();

        Path filePath = Paths.get("input", name + ".txt");

        // Crea la carpeta input si no existe
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {

            // Cabecera: tipo de documento aleatorio y numero de documento dado
            writer.write(Salesman.tipoDocumentoAleatorio() + ";" + id);
            writer.newLine();

            for (int i = 1; i <= randomSalesCount; i++) {
                Venta venta = new Venta(
                        idProductosValidos.get(RANDOM.nextInt(idProductosValidos.size())),
                        1 + RANDOM.nextInt(MAX_CANTIDAD_VENDIDA)
                );

                writer.write(
                        venta.getIdProducto() + ";" +
                                venta.getCantidadProductoVendido() + ";"
                );

                writer.newLine();
            }
        }
    }

    /*
     * Lee los IDs de producto desde input/productos.txt para que las ventas
     * solo referencien productos que realmente existen. Si el archivo no
     * existe, no se puede leer o esta vacio, se usa un rango por defecto
     * coherente con la cantidad de productos que genera main().
     */
    private static List<Integer> leerIdProductosValidos() {
        List<Integer> ids = new ArrayList<>();
        Path productsPath = Paths.get("input", "productos.txt");

        if (Files.exists(productsPath)) {
            try (BufferedReader reader = Files.newBufferedReader(productsPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] campos = line.split(";");
                    try {
                        ids.add(Integer.parseInt(campos[0].trim()));
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        // Linea mal formada: se omite y se sigue con la siguiente
                    }
                }
            } catch (IOException e) {
                // Si no se puede leer, se usa el rango por defecto
            }
        }

        if (ids.isEmpty()) {
            for (int i = 1; i <= PRODUCTS_COUNT; i++) {
                ids.add(i);
            }
        }
        return ids;
    }

    /*
     * Lee los vendedores desde input/vendedores.txt para generar
     * un archivo de ventas por cada uno.
     */
    private static List<Salesman> leerVendedores() throws IOException {
        List<Salesman> salesmen = new ArrayList<>();
        Path filePath = Paths.get("input", "vendedores.txt");

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] campos = line.split(";");
                salesmen.add(new Salesman(campos[0], campos[1], campos[2], campos[3]));
            }
        }
        return salesmen;
    }

    /*
     * Nombre de archivo de ventas derivado del vendedor:
     * "Nombres Apellidos" -> "Nombres_Apellidos" (sin espacios).
     */
    private static String nombreArchivoVentas(Salesman salesman) {
        return (salesman.getNombresVendedor() + "_" + salesman.getApellidosVendedor())
                .replaceAll("\\s+", "_");
    }
}