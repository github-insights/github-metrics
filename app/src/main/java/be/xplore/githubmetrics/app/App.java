package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.webApi.WebApiHello;
import be.xplore.githubmetrics.domain.DomainHello;
import be.xplore.githubmetrics.githubAdapter.GithubAdapterHello;

public class App {

    public static String hello() {
        return "Hello " + DomainHello.hello() + " " + WebApiHello.hello() + " " + GithubAdapterHello.hello();
    }
    public static void main(String[] args) {
        System.out.println(App.hello());
    }
}
