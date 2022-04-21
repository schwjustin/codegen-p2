package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;

import java.util.HashMap;

public class SymbolTable {

    //TODO:  Implement a symbol table class that is appropriate for this language.
    HashMap<String, Declaration> entries = new HashMap<>();
    String programName = "";
    //returns true if name successfully inserted in symbol table, false if already present
    public boolean insert(String name, Declaration declaration) {
        if (name == programName) return false;
        return (entries.putIfAbsent(name,declaration) == null);
    }

    public boolean remove(String name) {
        return (entries.remove(name) != null);
    }

    //returns Declaration if present, or null if name not declared.
    public Declaration lookup(String name) {
        return entries.get(name);
    }

    public void init(String name) {
        entries.get(name).setInitialized(true);
    }

}
