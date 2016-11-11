package de.tum.in.www1.exerciseapp.web.rest;

import de.tum.in.www1.exerciseapp.domain.File;
import de.tum.in.www1.exerciseapp.domain.Participation;
import de.tum.in.www1.exerciseapp.domain.Repository;
import de.tum.in.www1.exerciseapp.security.AuthoritiesConstants;
import de.tum.in.www1.exerciseapp.service.GitService;
import de.tum.in.www1.exerciseapp.service.ParticipationService;
import de.tum.in.www1.exerciseapp.web.rest.dto.RepositoryStatusDTO;
import de.tum.in.www1.exerciseapp.web.rest.util.HeaderUtil;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.StringContent;
import javax.swing.text.html.HTMLDocument;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Created by Josias Montag on 14.10.16.
 */
@RestController
@RequestMapping({"/api", "/api_basic"})
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class RepositoryResource {

    private final Logger log = LoggerFactory.getLogger(ParticipationResource.class);

    @Inject
    private GitService gitService;

    @Inject
    private ParticipationService participationService;

    private GrantedAuthority adminAuthority = new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN);
    private GrantedAuthority taAuthority = new SimpleGrantedAuthority(AuthoritiesConstants.TEACHING_ASSISTANT);


    @RequestMapping(value = "/repository/{id}/files",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<String>> getFiles(@PathVariable Long id, AbstractAuthenticationToken authentication) throws IOException, GitAPIException {
        log.debug("REST request to files for Participation : {}", id);
        Participation participation = participationService.findOne(id);

        if (!Optional.ofNullable(participation).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!participation.getStudent().getLogin().equals(authentication.getName()) && !(authentication.getAuthorities().contains(adminAuthority) && !authentication.getAuthorities().contains(taAuthority))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        Repository repository = gitService.getOrCheckoutRepository(participation);
        Iterator<File> itr = gitService.listFiles(repository).iterator();

        Collection<String> fileList = new LinkedList<String>();

        while (itr.hasNext()) {
            fileList.add(itr.next().toString());
        }

        return new ResponseEntity<>(
            fileList,
            HttpStatus.OK);
    }


    @RequestMapping(value = "/repository/{id}/file",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> getFile(@PathVariable Long id, @RequestParam("file")  String filename, AbstractAuthenticationToken authentication) throws IOException, GitAPIException {
        log.debug("REST request to file {} for Participation : {}", filename, id);
        Participation participation = participationService.findOne(id);

        if (!Optional.ofNullable(participation).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!participation.getStudent().getLogin().equals(authentication.getName()) && !(authentication.getAuthorities().contains(adminAuthority) && !authentication.getAuthorities().contains(taAuthority))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Repository repository = gitService.getOrCheckoutRepository(participation);

        Optional<File> file = gitService.getFileByName(repository, filename);

        if(!file.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        InputStream inputStream = new FileInputStream(file.get());

        byte[]out=org.apache.commons.io.IOUtils.toByteArray(inputStream);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);


        return new ResponseEntity(out, responseHeaders,HttpStatus.OK);

    }


    @RequestMapping(value = "/repository/{id}/file",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateFile(@PathVariable Long id, @RequestParam("file")  String filename, HttpServletRequest request, AbstractAuthenticationToken authentication) throws IOException, GitAPIException {
        log.debug("REST request to update file {} for Participation : {}", filename, id);
        Participation participation = participationService.findOne(id);

        if (!Optional.ofNullable(participation).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!participation.getStudent().getLogin().equals(authentication.getName()) && !(authentication.getAuthorities().contains(adminAuthority) && !authentication.getAuthorities().contains(taAuthority))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Repository repository = gitService.getOrCheckoutRepository(participation);

        Optional<File> file = gitService.getFileByName(repository, filename);

        if(!file.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        InputStream inputStream = request.getInputStream();

        Files.copy(inputStream, file.get().toPath(), StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("file", filename)).build();

    }



    @RequestMapping(value = "/repository/{id}/commit",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateFile(@PathVariable Long id, HttpServletRequest request, AbstractAuthenticationToken authentication) throws IOException, GitAPIException {
        log.debug("REST request to commit Repository for Participation : {}", id);
        Participation participation = participationService.findOne(id);

        if (!Optional.ofNullable(participation).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!participation.getStudent().getLogin().equals(authentication.getName()) && !(authentication.getAuthorities().contains(adminAuthority) && !authentication.getAuthorities().contains(taAuthority))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Repository repository = gitService.getOrCheckoutRepository(participation);

        gitService.stageAllChanges(repository);
        gitService.commitAndPush(repository, "Changes by Online Editor");

        return new ResponseEntity<>(HttpStatus.OK);

    }



    @RequestMapping(value = "/repository/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RepositoryStatusDTO> getStatus(@PathVariable Long id, HttpServletRequest request, AbstractAuthenticationToken authentication) throws IOException, GitAPIException {
        log.debug("REST request to get clean status for Repository for Participation : {}", id);
        Participation participation = participationService.findOne(id);

        if (!Optional.ofNullable(participation).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!participation.getStudent().getLogin().equals(authentication.getName()) && !(authentication.getAuthorities().contains(adminAuthority) && !authentication.getAuthorities().contains(taAuthority))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Repository repository = gitService.getOrCheckoutRepository(participation);

        RepositoryStatusDTO status = new RepositoryStatusDTO();

        status.isClean = gitService.isClean(repository);

        if(status.isClean) {
            gitService.pull(repository);
        }

        return new ResponseEntity<>(status, HttpStatus.OK);

    }





}
