import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TruthTableGUI {

    // Bool interface with logical operators
    interface Bool {
        boolean get();
        default Bool and(Bool r) { return () -> get() ? r.get() : false; }
        default Bool or(Bool r) { return () -> get() ? true : r.get(); }
        default Bool not() { return () -> !get(); }
    }

    // Truth table logic
    static class TruthTable {
        String formula;
        int index, ch;
        List<Character> vars;
        Map<Character, Boolean> map;
        Bool bool;
        StringBuilder output = new StringBuilder();

        int get() {
            return ch = index < formula.length() ? formula.charAt(index++) : -1;
        }

        boolean match(int expect) {
            if (ch == expect) {
                get();
                return true;
            }
            return false;
        }

        Bool element() {
            Bool b;
            if (match('(')) {
                b = expression();
                if (!match(')'))
                    throw new RuntimeException("')' expected");
            } else if (Character.isAlphabetic(ch)) {
                char v = (char) ch;
                get();
                if (!vars.contains(v))
                vars.add(v);
                b = () -> map.get(v);
            } else
                throw new RuntimeException("Unknown char: " + (char) ch);
            return b;
        }

        Bool factor() {
            if (match('~'))  // NOT
                return element().not();
            return element();
        }

        Bool term() {
            Bool b = factor();
            while (match('&'))  // AND
                b = b.and(factor());
            return b;
        }

        Bool expression() {
            Bool b = term();
            while (match('|'))  // OR
                b = b.or(term());
            return b;
        }

        String str(boolean b) {
            return b ? "T" : "F";
        }

        void print() {
            for (char v : vars)
                output.append(str(map.get(v))).append(" ");
            output.append(str(bool.get())).append("\n");
        }

        void test(int i) {
            if (i >= vars.size())
                print();
            else {
                char c = vars.get(i);
                map.put(c, true);
                test(i + 1);
                map.put(c, false);
                test(i + 1);
            }
        }

        public String make(String formula) {
            try {
                this.formula = formula.replaceAll("\\s", "");
                index = 0;
                vars = new ArrayList<>();
                map = new HashMap<>();
                output.setLength(0); // Clear output
                get();
                bool = expression();
                if (ch != -1)
                    throw new RuntimeException("Extra string: '" + formula.substring(index - 1) + "'");

                // Header
                for (char v : vars)
                    output.append(v).append(" ");
                output.append(formula).append("\n");

                test(0);
                return output.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }

    // GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Truth Table Generator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            JTextField inputField = new JTextField();
            JButton generateButton = new JButton("Generate Truth Table");
            JTextArea outputArea = new JTextArea();
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            outputArea.setEditable(false);

            panel.add(new JLabel("Enter formula (~, &, |, parentheses):"), BorderLayout.NORTH);
            panel.add(inputField, BorderLayout.CENTER);
            panel.add(generateButton, BorderLayout.EAST);
            frame.add(panel, BorderLayout.NORTH);
            frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);

            generateButton.addActionListener(e -> {
                String formula = inputField.getText();
                TruthTable table = new TruthTable();
                String result = table.make(formula);
                outputArea.setText(result);
            });

            frame.setVisible(true);
        });
    }
}
