# Github Metrics

> A highly configurable plug and play service to visualize all of your organizations
Actions, Pull requests, Self hosted Runners ...

Have you ever had to view multiple Github actions at the same time over multiple
projects? Github Metrics allows you to collect data from mulitple repositories 
into a single point of truth giving you insights into, workflow run results, 
run times, total count metrics and much more.

![example view](https://github-insights.github.io/images/workflow_runs_image_1.png)

## Getting Started

To get started pull the latest image from [our package repository](https://github.com/github-insights/github-metrics/pkgs/container/github-metrics)
and after setting the [minimal configuration](https://github-insights.github.io/configuration/minimal-config/) deploy it on your infrastructure
of choice. The application will expose all metrics on the `/actuator/prometheus`
endpoint. A more detailed step by step guide can be found [here](https://github-insights.github.io/getting-started/)

## Metrics

Looking for a list of all exposed data, check out the [metrics page](https://github-insights.github.io/metrics/).

## Configuration

Through environment variables Our service is is highly configurable, many of the values can be configured to your
liking. To find out what exactly can be configured go [here](https://github-insights.github.io/configuration/configuration/) to find a list of
all environment variables.


# dummy