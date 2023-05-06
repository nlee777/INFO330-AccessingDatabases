import java.util.*;
import java.sql.*;
public class TeamAnalyzer {
    // All the "against" column suffixes:
    static String[] types = {
        "bug","dark","dragon","electric","fairy","fight",
        "fire","flying","ghost","grass","ground","ice","normal",
        "poison","psychic","rock","steel","water"
    };

    public static void main(String... args) throws Exception {
        // Take six command-line parameters
        if (args.length < 6) {
            print("You must give me six Pokemon to analyze");
            System.exit(-1);
        }

        // This bit of JDBC magic I provide as a free gift :-)
        // The rest is up to you.
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:pokemon.db")) {
            ArrayList<String> names = new ArrayList<>();
            Statement all = con.createStatement();
            ResultSet result = all.executeQuery("Select name from pokemon;");
            while (result.next()) {
                names.add(result.getString("name"));
            }

            // iterate through each given pokemon from da command line bruh
            for (String arg : args) {
                print("Analyzing " + arg);
                ArrayList<String> strongList = new ArrayList<>();
                ArrayList<String> weakList = new ArrayList<>();
                String typeQuery = "";
                String nameQuery = "";

                // condition for figuring out if it's a name or pokedex number
                if (names.contains(arg)) {
                    Statement statementId = con.createStatement();
                    ResultSet idResult = statementId.executeQuery("Select pokedex_number from pokemon where name = '" + arg +"';");
                    String id = idResult.getString("pokedex_number");
                    nameQuery = "Select name from pokemon where pokedex_number = " + id + ";";
                    typeQuery = "Select * from pokemon_type where pokemon_id =" + id + " order by which asc;";
                } else {
                    nameQuery = "Select name from pokemon where pokedex_number = " + arg + ";";
                    typeQuery = "Select * from pokemon_type where pokemon_id =" + arg + " order by which asc;";
                }

                // gets types of current pokemon and stores the results in a ResultSet
                Statement statementType = con.createStatement();
                ResultSet typeResult = statementType.executeQuery(typeQuery);
                String type1 = typeResult.getString("type_id");
                typeResult.next();
                typeResult.next();
                String type2 = typeResult.getString("type_id");
                String againstQuery = "Select * from against where type_source_id1 =" + type1 + " and type_source_id2 = " + type2 +";";
                Statement statementAgainst = con.createStatement();
                ResultSet againstResult = statementAgainst.executeQuery(againstQuery);

                // iterates through all pokemon and checks whether given pokemon is strong or weak
                // and then adds to the respective strong or weak list
                for (String type : types) {
                    String against = againstResult.getString("against_" + type);
                    if (Double.parseDouble(against) > 1) {
                        strongList.add(type);
                    } else if (Double.parseDouble(against) < 1) {
                        weakList.add(type);
                    }
                }

                Statement statementName = con.createStatement();
                ResultSet nameResult = statementName.executeQuery(nameQuery);
                String name = nameResult.getString("name");
                String typeNameQuery = "Select name from type where id =" + type1 + " or id =" + type2;
                Statement statementTypeName = con.createStatement();
                ResultSet typeNameResult = statementTypeName.executeQuery(typeNameQuery);
                String type1Name = typeNameResult.getString("name");
                typeNameResult.next();
                typeNameResult.next();
                String type2Name = typeNameResult.getString("name");
                print(name + "(" + type1Name + " " + type2Name + ") is strong against " + strongList.toString() + " but weak against " + weakList.toString());

                // Analyze the pokemon whose pokedex_number is in "arg"

                // You will need to write the SQL, extract the results, and compare
                // Remember to look at those "against_NNN" column values; greater than 1
                // means the Pokemon is strong against that type, and less than 1 means
                // the Pokemon is weak against that type
            }

            String answer = input("Would you like to save this team? (Y)es or (N)o: ");
            if (answer.equalsIgnoreCase("Y") || answer.equalsIgnoreCase("YES")) {
                String teamName = input("Enter the team name: ");

                // Write the pokemon team to the "teams" table
                print("Saving " + teamName + " ...");
            }
            else {
                print("Bye for now!");
            }
            con.close();
        }        
    }

    /*
     * These are here just to have some symmetry with the Python code
     * and to make console I/O a little easier. In general in Java you
     * would use the System.console() Console class more directly.
     */
    public static void print(String msg) {
        System.console().writer().println(msg);
    }

    /*
     * These are here just to have some symmetry with the Python code
     * and to make console I/O a little easier. In general in Java you
     * would use the System.console() Console class more directly.
     */
    public static String input(String msg) {
        return System.console().readLine(msg);
    }
}
