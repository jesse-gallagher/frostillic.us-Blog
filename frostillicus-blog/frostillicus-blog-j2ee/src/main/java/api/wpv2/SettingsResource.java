package api.wpv2;

import api.wpv2.model.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.security.RolesAllowed;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

import static api.wpv2.WPUtil.JSONB;

/**
 * @see <a href="https://developer.wordpress.org/rest-api/reference/settings/">https://developer.wordpress.org/rest-api/reference/settings/</a>
 */
@Path("/wp/v2/settings")
@RolesAllowed("admin")
public class SettingsResource {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Settings {
        String title;
        String description;
        String timezone;
        @JsonbProperty("date_format")
        String dateFormat;
        @JsonbProperty("time_format")
        String timeFormat;
        @JsonbProperty("start_of_week")
        int startOfWeek;
        @Builder.Default
        String language = Locale.US.toString();
        @JsonbProperty("use_smilies")
        boolean useSmilies;
        @JsonbProperty("default_category")
        int defaultCategory;
        @JsonbProperty("default_post_format")
        String defaultPostFormat;
        @JsonbProperty("posts_per_page")
        @Builder.Default
        int postsPerPage = 10;
        @JsonbProperty("default_ping_status")
        CommentStatus defaultPingStatus;
        @JsonbProperty("default_comment_status")
        CommentStatus defaultCommentStatus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        Settings settings = Settings.builder()
                .title("frostillic.us")
                .dateFormat("yyyy-MM-ddThh:mm:ss")
                .startOfWeek(1)
                .defaultPingStatus(CommentStatus.closed)
                .defaultCommentStatus(CommentStatus.closed)
                .build();
        return Response.ok(JSONB.toJson(settings)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post() {
        // Meant to be a no-op for now
        return Response.ok().build();
    }
}
