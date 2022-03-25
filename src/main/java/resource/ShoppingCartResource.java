package resource;

import entity.ShoppingCart;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/v1/carts")
public class ShoppingCartResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getCarts() {
        return ShoppingCart.getAllShoppingCarts()
                .onItem().transform(Response::ok)
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getSingleCart(@PathParam("id") final Long id) {
        return ShoppingCart.findByShoppingCartId(id)
                .onItem().ifNotNull().transform(cart -> Response.ok(cart).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createShoppingCart(final ShoppingCart shoppingCart) {
        if (shoppingCart == null || shoppingCart.name == null) {
            throw new WebApplicationException("ShoppingCart name was not set on request.", 422);
        }
        return ShoppingCart.createShoppingCart(shoppingCart)
                .onItem().transform(id -> URI.create("/v1/carts/" + id.id))
                .onItem().transform(Response::created)
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @PUT
    @Path("{cartid}/{productid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> update(@PathParam("cartid") final Long id, @PathParam("productid") final Long product) {
        return ShoppingCart.addProductToShoppingCart(id, product)
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);

    }

    @DELETE
    @Path("{cartid}/{productid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("cartid") final Long id, @PathParam("productid") final Long product) {
        return ShoppingCart.deleteProductFromShoppingCart(id, product)
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }
}
