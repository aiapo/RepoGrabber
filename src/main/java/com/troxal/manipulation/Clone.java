package com.troxal.manipulation;

import com.troxal.pojo.RepoInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import java.io.File;
import java.util.List;

public class Clone {
    // Clone a repo based on it's url and name
    private static boolean cloneRepo(String url, String name){
        // We need to try/catch git issues
        try{
            // Get clean name for Windows
            String cleanPath = name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            // Clone repo with JGit
            Git.cloneRepository()
                    .setNoCheckout(true)
                    .setURI(url+".git")
                    .setDirectory(new File("repos/"+cleanPath))
                    .call();
            return true;
        } catch (InvalidRemoteException e) {
            System.out.println("[ERROR] InvalidRemoteException: \n"+e);
            return false;
        } catch (TransportException e) {
            System.out.println("[ERROR] TransportException: \n"+e);
            return false;
        } catch (GitAPIException e) {
            System.out.println("[ERROR] GitAPIException: \n"+e);
            return false;
        }
    }

    // Clone all repos
    public static void cloneRepos(List<RepoInfo> repos){
        for(int i=0;i<repos.size();i++) {
            System.out.println("** Cloning "+repos.get(i).getName());
            if(cloneRepo(repos.get(i).getUrl(),repos.get(i).getName()+"_"+repos.get(i).getId()))
                System.out.println("** Clone successful");
        }
    }
}
