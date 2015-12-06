/*
 * Copyright 2015 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redsaz.embeddedrest;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Handles Not Found exceptions, specifically for HTML output.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Provider
public class EmbeddedRestExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    HttpHeaders headers;
    @Context
    HttpServletRequest request;

    private Templater cfg;

    public EmbeddedRestExceptionMapper() {
    }

    @Inject
    public EmbeddedRestExceptionMapper(Templater templater) {
        cfg = templater;
    }

    @Override
    public Response toResponse(NotFoundException e) {
        if (headers.getAcceptableMediaTypes().contains(MediaType.TEXT_HTML_TYPE)) {
            return createHtmlResponse(e);
        }
        return Response.status(404)
                .entity("")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private Response createHtmlResponse(Throwable e) {
        Map<String, Object> root = new HashMap<>();
        String base = request.getContextPath();
        String dist = base + "/dist";
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", "Not Found");
        root.put("content", "error-404.ftl");
        String body = cfg.buildFromTemplate(root, "page.ftl");
        return Response.status(404)
                .entity(body)
                .type(MediaType.TEXT_HTML_TYPE)
                .build();
    }
}
