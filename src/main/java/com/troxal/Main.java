package com.troxal;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class Main {
    public static void main(String[] args) throws IOException {

        String authToken = getConfig.getKey();

        String variables = "{\"queryString\": \"is:public archived:false pushed:>2010-01-01 languages:java\",\"amountReturned\": 5}";

        RepoGrab rg = new RepoGrab(authToken,variables);

        for(int i=0;i<rg.getRepos().size();i++){
            try{
                Git.cloneRepository()
                        .setURI(rg.getRepo(i).getUrl()+".git")
                        .setDirectory(new File("repos/"+rg.getRepo(i).getName()))
                        .call();
            } catch (InvalidRemoteException e) {
                throw new RuntimeException(e);
            } catch (TransportException e) {
                throw new RuntimeException(e);
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
        }
    }
}