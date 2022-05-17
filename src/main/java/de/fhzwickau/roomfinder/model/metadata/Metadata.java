package de.fhzwickau.roomfinder.model.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Diese Annotation kennzeichnet ein Metadaten-Objekt für {@link de.fhzwickau.roomfinder.model.graph.node.Node} und
 * {@link de.fhzwickau.roomfinder.model.graph.edge.Edge}.
 * Diese werden vor allem für die Reflection-API benötigt: Um den Graphen in einer Datei zu persistieren, wird ein
 * Programm erstellt, in das alle Metadaten eingegeben werden müssen. Das Programm ermittelt anhand der
 * Annotationen, welche Metadaten wie angegeben werden müssen.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Metadata {

    /**
     * Gibt an, ob der Wert des Objekts null annehmen darf.
     */
    boolean nullable() default false;

    /**
     * Dies ist eine Beschreibung der Aussage des Objekts. Wird im UI des Erstellungsprogramms ausgegeben.
     */
    String description() default "";
}
