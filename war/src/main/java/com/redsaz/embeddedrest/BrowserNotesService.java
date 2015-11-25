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

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * An endpoint for accessing notes. The REST endpoints and browser endpoints are
 * identical; look at docs/endpoints.md for why.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
@Path("/notes")
public class BrowserNotesService {

    @Context
    private HttpServletRequest httpRequest;
    private NotesResource notesRes;
    private Templater cfg;

    public BrowserNotesService() {
    }

    @Inject
    public BrowserNotesService(NotesResource notesResource, Templater config) {
        notesRes = notesResource;
        cfg = config;
    }

    /**
     * Presents a web page of notes.
     *
     * @return Notes, by URI and title.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response listNotesBrowser() {
        String base = httpRequest.getContextPath();
        String dist = base + "/dist";
        List<Note> notes = notesRes.getNotes();
        Map<String, Object> root = new HashMap<>();
        root.put("notes", notes);
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", "Notes");
        root.put("content", "notes-list.ftl");
        try {
            Template temp = cfg.getCfg().getTemplate("page.ftl");
            StringWriter sw = new StringWriter();
            temp.process(root, sw);
            return Response.ok(sw.toString()).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load template: " + ex.getMessage(), ex);
        } catch (TemplateException ex) {
            throw new RuntimeException("Cannot process template: " + ex.getMessage(), ex);
        }
    }

    /**
     * Presents a web page for editing a specific note.
     *
     * @param id The id of the note.
     * @return Note edit page.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("{id}/edit")
    public Response editNoteByBrowser(@PathParam("id") long id) {
        String base = httpRequest.getContextPath();
        String dist = base + "/dist";
        Note note = notesRes.getNote(id);
        if (note == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Map<String, Object> root = new HashMap<>();
        root.put("note", note);
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", "Edit Note");
        root.put("content", "note-edit.ftl");
        try {
            Template temp = cfg.getCfg().getTemplate("page.ftl");
            StringWriter sw = new StringWriter();
            temp.process(root, sw);
            return Response.ok(sw.toString()).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load template: " + ex.getMessage(), ex);
        } catch (TemplateException ex) {
            throw new RuntimeException("Cannot process template: " + ex.getMessage(), ex);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.TEXT_HTML})
    public Response finishEditOrCreateNoteByBrowser(@FormParam("id") long id,
            @FormParam("title") String title, @FormParam("body") String body) {
        Note withId = new Note(id, null, title, body);
        List<Note> notes = Collections.singletonList(withId);
        if (id != 0) {
            notesRes.updateAll(notes);
        } else {
            notesRes.createAll(notes);
        }
        Response resp = Response.seeOther(URI.create("notes")).build();
        return resp;
    }

    /**
     * Presents a web page for creating a specific note.
     *
     * @return Note create page.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("create")
    public Response createNoteByBrowser() {
        String base = httpRequest.getContextPath();
        String dist = base + "/dist";
        Map<String, Object> root = new HashMap<>();
        root.put("base", base);
        root.put("dist", dist);
        root.put("title", "Crete Note");
        root.put("content", "note-create.ftl");
        try {
            Template temp = cfg.getCfg().getTemplate("page.ftl");
            StringWriter sw = new StringWriter();
            temp.process(root, sw);
            return Response.ok(sw.toString()).build();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load template: " + ex.getMessage(), ex);
        } catch (TemplateException ex) {
            throw new RuntimeException("Cannot process template: " + ex.getMessage(), ex);
        }
    }

    @POST
    @Path("delete")
    public Response deleteNoteByBrowser(@FormParam("id") long id) {
        notesRes.deleteNote(id);
        Response resp = Response.seeOther(URI.create("notes")).build();
        return resp;
    }

}