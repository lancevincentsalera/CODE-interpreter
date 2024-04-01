package Main.Nodes;

public class ProgramNode extends ASTNode {
    private final ASTNode variableDeclarations;
    private final ASTNode executableCode;

    public ProgramNode(ASTNode variableDeclarations, ASTNode executableCode) {
        this.variableDeclarations = variableDeclarations;
        this.executableCode = executableCode;
    }

    public ASTNode getVariableDeclarations() {
        return variableDeclarations;
    }

    public ASTNode getExecutableCode() {
        return executableCode;
    }

    public void displayOutput() {
        if(executableCode instanceof DisplayNode displayNode) {
            System.out.println(displayNode.getOutput());
        }
    }

}
