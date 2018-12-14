/**
 * Copyright © 2016-2018 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package api.wpv2;

import api.wpv2.model.CommentStatus;
import api.wpv2.model.GUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Post;
import model.PostRepository;

import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static api.wpv2.WPUtil.JSONB;

@Path("/wp/v2/posts")
public class PostsResource {
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WPPost {
        public enum Status {
            publish, future, draft, pending, Private
        }

        @Data @AllArgsConstructor @NoArgsConstructor
        public static class Title {
            private String rendered;
        }

        @Data @AllArgsConstructor @NoArgsConstructor
        public static class Content {
            private String rendered;
            @JsonbProperty("protected")
            private boolean isProtected;
        }

        private Date date;
        @JsonbProperty("date_gmt")
        private Date dateGmt;
        private GUID guid;
        private int id;
        private String link;
        private Date modified;
        @JsonbProperty("modified_gmt")
        private Date modifieGmt;
        private String slug;
        private Status status;
        private String type;
        private String password;
        private Title title;
        private String content;
        private int author;
        private String excerpt;
        @JsonbProperty("featured_media")
        private int featuredMedia;
        @JsonbProperty("comment_status")
        private CommentStatus commentStatus;
        @JsonbProperty("ping_status")
        private CommentStatus pingStatus;
        private String format;
        private Map<String, Object> meta;
        private boolean sticky;
    }

    @Inject
    PostRepository posts;

    UriInfo uriInfo;

    @GET
    public Response list() {
        return Response.ok().entity(JSONB.toJson(
            posts.homeList().stream()
                .map(this::toWPPost)
                .collect(Collectors.toList())
        )).build();
    }

    private WPPost toWPPost(Post post) {
        URI baseUri = uriInfo.getBaseUri();
        URI postUri = baseUri.resolve("posts/" + post.getPostId());

        return WPPost.builder()
            .date(post.getPosted())
            .dateGmt(post.getPosted())
            .guid(new GUID(post.getPostId()))
            .id(post.getPostIdInt())
            .link(postUri.toString())
            // TODO get real modified date
            .modified(post.getPosted())
            .modifieGmt(post.getPosted())
            .slug(post.getName())
            .status(toWPStatus(post.getStatus()))
            .title(new WPPost.Title(post.getTitle()))

            .build();
    }

    private WPPost.Status toWPStatus(Post.Status status) {
        switch(status) {
        case Draft:
            return WPPost.Status.publish;
        case Posted:
        default:
            return WPPost.Status.draft;
        }
    }
}
