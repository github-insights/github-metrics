package be.xplore.githubmetrics.prometheusexporter.features;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FeaturesAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturesAspect.class);

    @Around(
            "@within(featureAssociation) || @annotation(featureAssociation)"
    )
    public Object checkAspect(ProceedingJoinPoint joinPoint,
                              FeatureAssociation featureAssociation) throws Throwable {

        if (featureAssociation.value().isActive()) {
            return joinPoint.proceed();
        } else {
            LOGGER.debug("Feature {} is not enabled!", featureAssociation.value().name());
            return null;
        }
    }
}
