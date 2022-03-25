package entity;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.persistence.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cacheable
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    private String title;

    public String description;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    @CreationTimestamp
    public ZonedDateTime createdAt;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    @UpdateTimestamp
    public ZonedDateTime updatedAt;

    public static Uni<Product> findByProductId(final Long id) {
        return findById(id);
    }

    public static Uni<Product> updateProduct(final Long id, final Product product) {
        return Panache.withTransaction(() -> findByProductId(id)
                        .onItem().ifNotNull()
                        .transform(entity -> {
                            entity.description = product.description;
                            entity.title = product.title;
                            return entity;
                        })
                        .onFailure().recoverWithNull());
    }

    public static Uni<Product> addProduct(final Product product) {
        return Panache.withTransaction(product::persist)
                .replaceWith(product)
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .transform(IllegalStateException::new);
    }

    public static Uni<List<Product>> getAllProducts() {
        return Product.listAll(Sort.by("createdAt"))
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .recoverWithUni(Uni.createFrom().<List<PanacheEntityBase>>item(Collections.EMPTY_LIST));

    }

    public static Uni<Boolean> deleteProduct(final Long id) {
        return Panache.withTransaction(() -> deleteById(id));
    }

    public String toString() {
        return this.getClass().getSimpleName() + "<" + this.id + ">";
    }

}
