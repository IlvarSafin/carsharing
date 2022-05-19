import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainAp {

	public static void main(String[] args) {
		String createCompany = "CREATE TABLE IF NOT EXISTS COMPANY" +
				"(id SERIAL NOT NULL, " +
				" name VARCHAR(255) NOT NULL UNIQUE, " +
				" PRIMARY KEY ( id ))";

		String createCar =    "CREATE TABLE IF NOT EXISTS CAR" +
				"(id SERIAL PRIMARY KEY, " +
				" name VARCHAR(255) NOT NULL UNIQUE, " +
				" company_id INTEGER NOT NULL," +
				" FOREIGN KEY (company_id)" +
				" REFERENCES COMPANY(id));";

		String createCustomer =   "CREATE TABLE IF NOT EXISTS CUSTOMER" +
				"(id SERIAL PRIMARY KEY, " +
				" name VARCHAR(255) NOT NULL UNIQUE, " +
				" rented_car_id INTEGER," +
				" FOREIGN KEY (rented_car_id)" +
				" REFERENCES CAR(id));";
		try(Connection connection = ConnectionManager.open();
		    Statement statement = connection.createStatement()) {
			statement.execute(createCompany);
			statement.execute(createCar);
			statement.execute(createCustomer);
			menu(statement);
		}
		catch (SQLException e){
			e.printStackTrace();
		}

	}

	public static void menu(Statement statement){
		Scanner scanner = new Scanner(System.in);
		System.out.println("1) Manager\n2) Customer\n3) Delete\n0) Exit");
		System.out.print("Chose: ");
		int chose = scanner.nextInt();
		if (chose == 1)
			manager(statement);
		else if(chose == 2)
			customer(statement);
		else if(chose == 3)
			delete(statement);
		else if(chose == 0)
			System.exit(0);
	}

	private static void delete(Statement statement) {
		System.out.println("1) Delete company\n0) Back");
		System.out.print("Chose: ");
		Scanner scanner = new Scanner(System.in);
		int chose = scanner.nextInt();
		if (chose == 1){
			deleteCompanyOne(statement);
		}
		else if(chose == 0){
			menu(statement);
		}
	}

	private static void deleteCompanyOne(Statement statement) {
		companyList(statement);
		System.out.println("0) Back");
		System.out.print("Chose: ");
		Scanner scanner = new Scanner(System.in);
		String companyName = scanner.nextLine();
		if(companyName.equals("0"))
			delete(statement);
		int companyId = 0;
		boolean check = false;
		try {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM company where name='" + companyName + "'");
			while (resultSet.next()){
				if (resultSet.getString("name").equals(companyName)) {
					companyId = resultSet.getInt("id");
					check = true;
				}
			}
			if (!check){
				System.out.println("Not available");
				delete(statement);
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		deleteCompanyTwo(statement, companyId);
	}

	private static void deleteCompanyTwo(Statement statement, int companyId) {
		System.out.println("1) All company\n2) Car list\n3) Customer\n0) Back");
		System.out.print("Chose");
		int chose = new Scanner(System.in).nextInt();
		if (chose == 1){
			deleteAllCompany(statement, companyId);
		}
		else if (chose == 2){
			deleteCar(statement, companyId);
		}
		else if (chose == 3){
			deleteCustomer(statement, companyId);
		}
		else if (chose == 0){
			deleteCompanyOne(statement);
		}
	}

	private static void deleteCustomer(Statement statement, int companyId) {
		try{
			ResultSet resultSet = statement.executeQuery("select customer.name, car.name FROM customer " +
					"join car on customer.rented_car_id = car.id where car.company_id=" + companyId);
			while (resultSet.next()){
				System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
			}
			System.out.print("Chose name customer: ");
			String chose = new Scanner(System.in).nextLine();
			resultSet = statement.executeQuery("select customer.name, car.name FROM customer " +
					"join car on customer.rented_car_id = car.id where car.company_id=" + companyId);
			while (resultSet.next()){
				if(resultSet.getString(1).equals(chose)){
					statement.execute("DELETE FROM customer where name='" + chose + "'");
					System.out.println("Complete");
					deleteCompanyTwo(statement, companyId);
				}
			}
			deleteCompanyTwo(statement, companyId);
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	private static void deleteCar(Statement statement, int companyId) {
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * FROM car where company_id=" + companyId);
			while (resultSet.next()){
				System.out.println(resultSet.getString("name"));
			}
			System.out.println("Chose:");
			String chose = new Scanner(System.in).nextLine();
			resultSet = statement.executeQuery("SELECT * FROM car where name='" + chose + "'");
			int carId = 0;
			if(resultSet.next())
				carId = resultSet.getInt("id");
			resultSet = statement.executeQuery("SELECT * FROM customer where rented_car_id=" + carId);
			if(resultSet.next()){
				System.out.println("Not available!");
				deleteCompanyTwo(statement, companyId);
			}
			resultSet = statement.executeQuery("SELECT * FROM car where company_id=" + companyId);
			while (resultSet.next()){
				if (resultSet.getString("name").equals(chose)){
					statement.execute("delete from car where name='" + chose + "'");
					System.out.println("Complete");
				}
			}
			deleteCompanyTwo(statement, companyId);
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	private static void deleteAllCompany(Statement statement, int companyId) {
		List<Integer> carIdList = new ArrayList<>();
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * from car where company_id=" + companyId);
			while (resultSet.next()){
				carIdList.add(resultSet.getInt("id"));
			}

			resultSet = statement.executeQuery("SELECT * from customer");
			while (resultSet.next()){
				if(carIdList.contains(resultSet.getInt("rented_car_id"))){
					System.out.println("Not available: customer");
					deleteCompanyTwo(statement, companyId);
				}
			}
			statement.execute("DELETE FROM car where company_id=" + companyId);
			statement.execute("DELETE FROM company where id=" + companyId);
			System.out.println("Complete");
			deleteCompanyOne(statement);
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static void manager(Statement statement) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("1) Create company\n2) Chose company\n0) Back");
		System.out.print("Chose: ");
		int chose = scanner.nextInt();
		if (chose == 1){
			try{
				Scanner scanner1 = new Scanner(System.in);
				System.out.print("Company name: ");
				String companyName = scanner1.nextLine();
				ResultSet resultSet = statement.executeQuery("SELECT * FROM company where name='" + companyName + "'");
				while (resultSet.next()){
					if (resultSet.getString("name").equals(companyName)) {
						System.out.println("Not available");
						manager(statement);
					}
				}
				String createCompany = "INSERT INTO company(name)" +
						"VALUES ('" + companyName + "');";
				statement.execute(createCompany);
				System.out.println("Company was found!");
				manager(statement);
			}
			catch (SQLException e){
				e.printStackTrace();
			}
		}
		else if(chose == 2) {
			chose_company(statement);
		}
		else if(chose == 0)
			menu(statement);
	}

	private static void chose_company(Statement statement) {
		companyList(statement);
		System.out.println("0) Back");
		System.out.print("Chose company: ");
		Scanner scanner = new Scanner(System.in);
		String sur = scanner.nextLine();
		if (sur.equals("0"))
			manager(statement);
		int id = 0;
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * FROM COMPANY");
			while (resultSet.next()){
				if (resultSet.getString("name").equals(sur)){
					id = resultSet.getInt("id");
				}
			}
			carList(statement, id);
		}
		catch (SQLException e){
			e.printStackTrace();
		}

	}

	private static void companyList(Statement statement){
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * FROM COMPANY");
			while (resultSet.next()){
				System.out.println(resultSet.getString("name"));
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	private static void carList(Statement statement, int id) {
		System.out.println("1) Create car\n2) Car List\n0) Back");
		Scanner scanner = new Scanner(System.in);
		System.out.print("Chose: ");
		int chose = scanner.nextInt();
		if (chose == 1){
			System.out.print("Add model: ");
			Scanner scanner1 = new Scanner(System.in);
			String model = scanner1.nextLine();
			ResultSet resultSet = null;
			try {
				resultSet = statement.executeQuery("SELECT * FROM car where name='" + model + "' and company_id=" + id);
				while (resultSet.next()){
					if (resultSet.getString("name").equals(model)) {
						System.out.println("Not available");
						chose_company(statement);
					}
				}
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
			String sql = "INSERT INTO car(name, company_id)" +
					"VALUES ('" + model +"', " + id + ");";
			try {
				statement.execute(sql);
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
			System.out.println("Model complete!");
			carList(statement,  id);
		}
		else if (chose == 2){
			try{
				ResultSet resultSet = statement.executeQuery("SELECT * FROM car where company_id=" + id);
				while (resultSet.next()){
					System.out.println(resultSet.getString("name"));
				}
			}
			catch (SQLException e){
				e.printStackTrace();
			}
			System.out.println("Press key and ENTER!");
			Scanner scanner1 = new Scanner(System.in);
			scanner1.nextLine();
			carList(statement, id);
		}
		else if(chose == 0){
			manager(statement);
		}
	}

	public static void customer(Statement statement){
		System.out.println("\n1) Chose car\n0) Back");
		Scanner scanner = new Scanner(System.in);
		System.out.print("Chose: ");
		int chose = scanner.nextInt();
		if (chose == 1){
			choseCar(statement);
		}else if (chose == 0){
			menu(statement);
		}
	}

	private static void choseCar(Statement statement) {
		companyList(statement);
		System.out.println("0) Back");
		System.out.print("Chose company: ");
		Scanner scanner = new Scanner(System.in);
		String companyName = scanner.nextLine();
		if (companyName.equals("0")){
			customer(statement);
		}
		int companyId = 0;
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * FROM COMPANY where name='" + companyName + "'");
			while (resultSet.next()){
				if (resultSet.getString("name").equals(companyName)){
					companyId = resultSet.getInt("id");
				}
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		System.out.println();
		listCar(statement, companyId);
	}

	private static void listCar(Statement statement, int companyId){
		int carId = 0;
		try{
			ResultSet resultSet = statement.executeQuery("SELECT * FROM car where company_id=" + companyId);
			while (resultSet.next()){
				System.out.println(resultSet.getString("name"));
			}
			System.out.println("0) Back");
			Scanner scanner = new Scanner(System.in);
			System.out.print("Chose model: ");
			String car = scanner.nextLine();
			if (car.equals("0"))
				choseCar(statement);
			ResultSet resultSet1 = statement.executeQuery("SELECT * FROM car where name='" + car + "'");
			while (resultSet1.next()){
				carId = resultSet1.getInt("id");
			}
			if (checkCarIdOnRented(statement, carId)){
				System.out.println("open");
				carsharing(statement, companyId, carId);
			}
			else {
				System.out.println("close");
				listCar(statement, companyId);
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}

	}

	private static boolean checkCarIdOnRented(Statement statement, int carId) {
		try {
			ResultSet resultSet = statement.executeQuery("SELECT * from customer where rented_car_id=" + carId);
			while(resultSet.next()){
				if (resultSet.getInt("rented_car_id") == carId)
					return false;
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return true;
	}

	private static void carsharing(Statement statement, int companyId, int carId){
		System.out.println("Your name: ");
		Scanner scanner = new Scanner(System.in);
		String customerName = scanner.nextLine();
		int customerId = 0;
		try {
			String sql = "INSERT INTO customer(name) " +
			"values('" + customerName + "')";
			statement.execute(sql);
			ResultSet resultSet = statement.executeQuery("SELECT * FROM customer where name='" + customerName + "'");
			while (resultSet.next()){
				customerId = resultSet.getInt("id");
			}
			sql = "UPDATE customer " +
					"SET rented_car_id=" + carId +
					" where name = '" + customerName + "'";
			statement.execute(sql);
			System.out.println("You rented!");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
