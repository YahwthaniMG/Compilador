package javiergs.compiler;

import java.util.*;
import javiergs.compiler.*;

/**
 * Automata.java
 * ---------------
 * Implements a Deterministic Finite Automaton (DFA) using a HashMap-based transition table.
 * This class provides the core functionality for defining and executing a DFA, which is used
 * for lexical analysis and pattern recognition in the compiler's front-end.
 *
 * The DFA implementation supports:
 * - Adding transitions between states
 * - Defining accept states
 * - Checking if a state is an accept state
 * - Retrieving the next state based on current state and input
 *
 * @author javiergs
 * @author eduardomv
 * @author santiarr
 * @author yawham
 * @version 2.0
 */
public class Automata {

    private final HashMap<String, String> table = new HashMap<>();
    private final HashMap<String, String> acceptStates = new HashMap<>();

    /**
     * Adds a transition to the DFA's transition table.
     *
     * @param currentState The starting state for this transition
     * @param inputSymbol The input symbol that triggers this transition
     * @param nextState The state to transition to
     */
    public void addTransition(String currentState, String inputSymbol, String nextState) {
        table.put(currentState + "/" + inputSymbol, nextState);
    }

    /**
     * Determines the next state based on the current state and input symbol.
     *
     * @param currentState The current state of the DFA
     * @param inputSymbol The input symbol being processed
     * @return The next state according to the transition table
     */
    public String getNextState(String currentState, char inputSymbol) {
        return table.get(currentState + "/" + inputSymbol);
    }

    /**
     * Adds an accept state to the DFA with its associated token type name.
     *
     * @param state The state to be marked as an accept state
     * @param name The token type name associated with this accept state
     */
    public void addAcceptState(String state, String name) {
        acceptStates.put(state, name);
    }

    /**
     * Checks if a given state is an accept state.
     *
     * @param name The state to check
     * @return true if the state is an accept state, false otherwise
     */
    public boolean isAcceptState(String name) {
        return acceptStates.containsKey(name);
    }

    /**
     * Gets the token type name associated with an accept state.
     *
     * @param state The accept state
     * @return The token type name associated with the state
     */
    public String getAcceptStateName(String state) {
        return acceptStates.get(state);
    }

    /**
     * Prints the DFA's transition table in a GraphViz-compatible format.
     * Each line represents a transition in the format:
     * currentState -> nextState [label="inputSymbol"];
     */
    public void printTable() {
        System.out.println("DFA Transition Table:");
        for (String state : table.keySet()) {
            String[] parts = state.split("/");
            System.out.println(parts[0] + " -> " + table.get(state) + " [label=\"" + parts[1] + "\"];");
        }
    }

}

