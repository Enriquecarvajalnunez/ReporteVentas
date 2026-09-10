import java.io.IOException;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class GenerateInfoFiles {
    public static void main(String[] args){
        try{
            createProductsFile(0);
            System.out.println("Archivo de productos generado correctamente.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }catch (IllegalArgumentException ext){
            System.out.println(ext.getMessage());
        }

    }

    public static void createProductsFile(int productsCount)throws IOException{

        //validaciones Guard
        if(productsCount <= 0){
            throw new IllegalArgumentException(
              "La cantidad de productos debe ser mayor que cero"
            );
        }

        Random random = new Random();
        //Crea objeto patch, construye y guarda la ruta.
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
}


