package be.xplore.githubmetrics.app;

import be.xplore.githubmetrics.domain.DomainHello;
import be.xplore.githubmetrics.githubadapter.GithubAdapterHello;
import be.xplore.githubmetrics.prometheusexporter.PrometheusExporterHello;

public class App {

    public static String hello() {
        return "Hello " + DomainHello.hello() + " " + PrometheusExporterHello.hello() + " " + GithubAdapterHello.hello();
    }

    public static void main(String[] args) {
        System.out.println(App.hello());
    }
}
