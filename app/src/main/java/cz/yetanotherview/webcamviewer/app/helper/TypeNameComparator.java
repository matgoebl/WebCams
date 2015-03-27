package cz.yetanotherview.webcamviewer.app.helper;

import java.util.Comparator;

import cz.yetanotherview.webcamviewer.app.model.Type;

public class TypeNameComparator implements Comparator<Type> {
    @Override
    public int compare(Type type1, Type type2) {
        String typeName1 = type1.getTypeName();
        String typeName2 = type2.getTypeName();

        return typeName1.compareToIgnoreCase(typeName2);
    }
}
