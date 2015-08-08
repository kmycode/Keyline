package jp.asfres.lib.db;

/**
 * Keyline
 * jp.asfres.lib.db @ IDB.java
 * 作成日時：2015/05/31 14:40:20
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * データベースをあらわすクラスです。
 * すべてのデータベースクラスは、このクラスを実装します。<br>
 * このクラスは、どの種類のデータベースにも対応しています。
 * したがって、それぞれのデータベースに固有のコードを書く場合は、
 * このクラスを実装したクラスにその処理を記述しなければいけません。
 *
 * @author kmy
 * @version 1.1
 * @since 1.0
 */
public abstract class MyDB {

	/** データベースへの接続をあらわすオブジェクト */
	protected Connection connection = null;

	/** データベースのステートメントをあらわすオブジェクト */
	protected Statement statement = null;

	/** 動的クラスのロードを試みたかをあらわすフラグ */
	protected static boolean isClassLoad = false;

	/** データベース接続のタイムアウト時間 */
	public static final int TIMEOUT_SECOND = 30;

	/** データベースログインID */
	protected String id = "";

	/** データベースログインパスワード */
	protected String password = "";

	/** システムテーブル */
	private static QueryBuilder.Table TABLE = new QueryBuilder.Table("system_key").notnull(true).character("key", 64)
			.primarykey().int32("value");

	/**
	 * データベースサーバーに接続します。
	 * 接続に失敗した場合は、例外SQLExceptionをスローします。<br>
	 * このメソッドに期待されることは、ConnectionとStatementオブジェクトへのインスタンスをフィールドに格納することです。<br>
	 * データベースのサーバー名、ポート番号、IDとパスワードなど、具体的な接続情報は、このメソッドの実装者が設定する必要があります。
	 *
	 * @version 1.0
	 * @since 1.0
	 */
	public abstract void connect() throws SQLException;

	/**
	 * データベースを選択します。
	 * 選択に失敗した場合は、例外SQLExceptionをスローします。
	 *
	 * @param dbname
	 *            データベース名
	 * @version 1.0
	 * @since 1.0
	 */
	public void selectDB(String dbname) throws SQLException {
		// データベースへ接続
		this.connection = DriverManager.getConnection(this.getDBName(dbname));
		this.statement = this.connection.createStatement();

		// タイムアウト時間を設定
		this.statement.setQueryTimeout(MyDB.TIMEOUT_SECOND);
	}

	/**
	 * データベースへのログイン情報を設定します。
	 * このメソッドは、selectDB()でデータベースへ接続する前に呼び出してください。
	 * SQLiteなど、ログイン情報が不要のデータベースでは、このメソッドで設定した内容は無効になります。
	 *
	 * @param id
	 * @param pass
	 * @version
	 * @since
	 */
	public void login(String id, String pass) {
		this.id = id;
		this.password = pass;
	}

	/**
	 * データベースを選択するとき、接続情報に渡す文字列を設定します。
	 * この部分は、データベースの種類によって異なります。
	 *
	 * @param dbname
	 *            データベース名
	 * @return データベース名をもとにした、JDBC接続に必要な文字列
	 * @version 1.0
	 * @since 1.0
	 */
	protected abstract String getDBName(String dbname);

	/**
	 * データベースをクローズします。
	 * クローズに失敗した場合は、例外SQLExceptionをスローします。<br>
	 * データベースを使用した時は、必ず使用者が明示的にこのメソッドを実行してください。
	 *
	 * @throws SQLException
	 *             データベースとのやり取りに失敗した場合、この例外がスローされます。
	 * @version 1.0
	 * @since 1.0
	 */
	public void close() throws SQLException {
		if (this.statement != null) {
			this.statement.close();
		}
		if (this.connection != null) {
			this.connection.close();
		}
	}

	/**
	 * このメソッドは、ファイナライザです。明示的に呼び出す必要はありません。<br>
	 * このメソッドは、データベースを閉じます。通常は、データベースクラスの使用者がデータベースへの接続が不要となった時に
	 * <code>close</code>メソッドを呼び出すことを期待しているのですが、
	 * プログラムの書き間違いなどによってクローズし忘れた場合、このメソッドによって処理を行います。<br>
	 * デストラクタではなくファイナライザである以上、このメソッドに依存しないプログラムを記述する必要があります。
	 *
	 * @see java.lang.Object#finalize()
	 * @version 1.0
	 * @since 1.0
	 */
	@Override
	protected final void finalize() throws Throwable {
		try {
			super.finalize();
		}
		finally {
			this.close();
		}
	}

	/**
	 * 指定したサイズにあった整数型名を返します。
	 * データベースの種類によって返される文字列が異なります。
	 * このメソッドは、create table文などで用いられます。<br>
	 * サイズは、2進数の桁数で指定します。
	 * また、その型のカラムが取りうる最大値を考慮する必要があります。
	 *
	 * @param size
	 *            型に必要なサイズを2進の桁数で指定
	 * @return サイズに見合った型の名前
	 * @version 1.0
	 * @since 1.0
	 */
	public abstract String intType(int size);

	/**
	 * 指定したサイズにあった文字列型名を返します。
	 * データベースの種類によって返される文字列が異なります。
	 * このメソッドは、create table文などで用いられます。<br>
	 * サイズは、文字列のバイト数で指定します。
	 * また、その型のカラムが取りうる最大文字列長を考慮する必要があります。
	 *
	 * @param size
	 *            型に必要なサイズをバイト数で指定
	 * @return サイズに見合った型の名前
	 * @version 1.0
	 * @since 1.0
	 */
	public abstract String stringType(int size);

	/**
	 * 日付、時刻両方の情報をとる日時型名を返します。
	 * データベースの種類によって返される文字列が異なります。
	 * このメソッドは、create table文などで用いられます。
	 *
	 * @return 型の名前
	 * @version 1.0
	 * @since 1.0
	 */
	public abstract String datetimeType();

	/**
	 * 指定されたクエリを実行し、その結果を返します。
	 * 返される結果が１つのみである場合を想定しています。
	 * 複数行・複数列にいたる結果が取得されることが想定される場合は、以下のようにしてください。
	 * <code>rsmd.getColumnCount()</code>によって列、<code>while(rs.next())</code>
	 * によって行をカウントすることができます。
	 *
	 * <pre>
	 * ResultSet rs = query(sql);
	 * ResultSetMetaData rsmd = rs.getMetaData();
	 * while (rs.next()) {
	 * 	for (int i = 1; i &lt;= rsmd.getColumnCount(); i++) {
	 * 		System.out.print(rs.getString(i));
	 * 		System.out.print(i &lt; rsmd.getColumnCount() ? &quot;,&quot; : &quot;&quot;);
	 * 	}
	 * 	System.out.print(System.getProperty(&quot;line.separator&quot;));
	 * }
	 * </pre>
	 *
	 * 実行に失敗した場合は、例外SQLExceptionをスローします。
	 *
	 * @param q
	 *            クエリ。SELECT文など
	 * @return クエリに成功すれば実行結果。失敗すればnull
	 * @version 1.0
	 * @since 1.0
	 */
	public ResultSet query(String q) throws SQLException {
		ResultSet r = null;

		// 例外SQLException
		r = this.statement.executeQuery(q);

		return r;
	}

	/**
	 * 指定されたクエリを実行し、その結果を返します。
	 * 返される結果がない場合を想定しています。
	 * 実行に失敗した場合は、例外SQLExceptionをスローします。
	 *
	 * @param q
	 *            クエリ。INSERT文、UPDATE文、DELETE文など
	 * @return クエリに成功すれば0または正の整数。失敗した場合は-1
	 * @version 1.0
	 * @since 1.0
	 */
	public int queryNoResult(String q) throws SQLException {
		int r = -1;

		// 例外SQLException
		r = this.statement.executeUpdate(q);

		return r;
	}

	/**
	 * クエリビルダのオブジェクトを直接渡し、クエリを実行します。
	 * どのタイプのメソッドを実行するか、このメソッドが自動で決定します。
	 *
	 * @param qb
	 *            クエリビルダ
	 * @return ResultSetオブジェクト（ない場合はnull）
	 * @throws SQLException
	 *             クエリの実行に失敗した場合、この例外がスローされます。
	 * @version 1.0
	 * @since 1.0
	 */
	public ResultSet query(QueryBuilder qb) throws SQLException {
		int queryType = qb.getQueryType();
		if (queryType == QueryBuilder.SELECT) {
			return this.query(qb.toString());
		}
		else {
			this.queryNoResult(qb.toString());
			return null;
		}
	}

	/**
	 * 指定した名前のテーブルが存在するか確認します。
	 *
	 * @param tableName
	 *            存在を確認するテーブル名
	 * @return テーブルが存在すればtrue、存在しなければfalse。データ取得に失敗した場合にもfalse
	 * @version 1.0
	 * @since 1.0
	 */
	public abstract boolean isTableExist(String tableName) throws SQLException;

	/**
	 * システムテーブルから任意の名前がついた設定値を取得し、返します。
	 * システムテーブルが存在しない場合は、新たにテーブルを作成します。
	 * 指定された名前のレコードがシステムテーブルに存在しない場合は、例外SQLExceptionをスローします。
	 *
	 * @param varName
	 *            パラメータ名
	 * @return データベースのシステムテーブルに登録されている、指定した名前に対応した設定値
	 * @version 1.0
	 * @since 1.0
	 */
	public final int getSystemValue(String varName) throws SQLException {

		ResultSet rs = null;
		int result;

		// 値を取得するためのクエリを発行
		// rs = this.query("select `value` from `system_key` where `key` = '" +
		// varName + "';");
		rs = new QueryBuilder().select().column("value").from(MyDB.TABLE).where("key", "like", varName).exe(this);
		rs.next();

		// 結果を数値として取得
		result = rs.getInt(1);

		return result;
	}

	/**
	 * システムテーブルから任意の名前がついた設定値を取得し、返します。
	 * システムテーブルが存在しない場合は、新たにテーブルを作成します。
	 * 指定された名前のレコードがシステムテーブルに存在しない場合は、例外SQLExceptionをスローします。
	 *
	 * @param varName
	 *            パラメータ名
	 * @version 1.0
	 * @since 1.0
	 */
	public final void setSystemValue(String varName, int value) throws SQLException {

		ResultSet rs = null;

		try {
			// 設定する前にレコードの存在を確認する。レコードがなければ新規作成
			// レコードが無くでもupdateでエラーは起きないので、毎回チェックする
			// rs =
			// this.query("select count(*) from `system_key` where `key` like '"
			// + varName + "';");
			rs = new QueryBuilder().select().column("count(*)").from(MyDB.TABLE).where("key", "like", varName)
					.exe(this);
			rs.next();
			if (rs.getInt(1) <= 0) {
				// this.queryNoResult("insert into `system_key` values('" +
				// varName + "', " + value + ");");
				new QueryBuilder().insert().setTable(MyDB.TABLE).push().set("key", varName).set("value", value)
						.exe(this);
			}

			// 値を設定するためのクエリを発行
			// this.queryNoResult("update `system_key` set `value`=" + value +
			// " where `key` like '" + varName + "';");
			new QueryBuilder().update().setTable(MyDB.TABLE).where("key", "like", varName).push().set("value", value)
			.exe(this);
		}
		catch (SQLException e) {

			// エラーの原因がこれから調べるものの中から見つからなかったか？
			boolean isUnknownError = false;

			// 以下のステップでエラーが発生した時、メソッドのthrowsで呼び出し側にスローされる

			// エラーの原因はテーブルがなかったからであるか確認する。なければ新規作成
			if (!this.isTableExist("system_key")) {
				this.queryNoResult("create table `system_key`(key varchar(64) primary key, value int);");
			}
			else {
				isUnknownError = true;
			}

			// 未知のエラーによるもの
			if (isUnknownError) {
				throw e;
			}
		}
	}

	/**
	 * オートインクリメントで最後に追加されたIDを取得します。
	 *
	 * @param tableName
	 *            テーブル名
	 * @return 最後に追加されたID
	 * @throws SQLException
	 *             データベースとのやり取りに失敗した場合、この例外がスローされます。
	 * @version 1.1
	 * @since 1.1
	 */
	public int lastAi(String tableName) throws SQLException {
		ResultSet rs = new QueryBuilder().select().func("last_insert_id", "").from(tableName).exe(this);
		if (rs.next()) {
			return rs.getInt(1);
		}
		else {
			throw new SQLException();
		}
	}

	/**
	 * オートインクリメントで最後に追加されたIDを取得します。
	 *
	 * @param table
	 *            テーブルオブジェクト
	 * @return 最後に追加されたID
	 * @throws SQLException
	 *             データベースとのやり取りに失敗した場合、この例外がスローされます。
	 * @version 1.1
	 * @since 1.1
	 */
	public int lastAi(QueryBuilder.Table table) throws SQLException {
		return this.lastAi(table.getTableName());
	}

	/**
	 * 日時をMySQLで利用できる文字列に変換します。
	 *
	 * @param cal
	 *            文字列に変換する日時
	 * @return MySQLで利用できる形式に変換された日時
	 * @version 1.1
	 * @since 1.1
	 */
	public static String datetimeToString(Calendar cal) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(cal.getTime());
	}

	/**
	 * 日付をMySQLで利用できる文字列に変換します。
	 *
	 * @param cal
	 *            文字列に変換する日付
	 * @return MySQLで利用できる形式に変換された日付
	 * @version 1.1
	 * @since 1.1
	 */
	public static String dateToString(Calendar cal) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(cal.getTime());
	}

	/**
	 * 時刻をMySQLで利用できる文字列に変換します。
	 *
	 * @param cal
	 *            文字列に変換する時刻
	 * @return MySQLで利用できる形式に変換された時刻
	 * @version 1.1
	 * @since 1.1
	 */
	public static String timeToString(Calendar cal) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		return format.format(cal.getTime());
	}

	/**
	 * MySQLのフォーマットに沿った日時をあらわす文字列を、カレンダーオブジェクトに変換します。
	 *
	 * @param str
	 *            MySQLのフォーマットにしたがった日時をあらわす文字列
	 * @return カレンダーオブジェクト
	 * @version 1.1
	 * @since 1.1
	 */
	public static Calendar stringToDatetime(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = new Date(format.parse(str).getTime());
		}
		catch (ParseException e) {
			System.err.println("日付変換でエラーが発生しました");
			e.printStackTrace();
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * MySQLのフォーマットに沿った日付をあらわす文字列を、カレンダーオブジェクトに変換します。
	 * 時刻は０時０分０秒となります。
	 *
	 * @param str
	 *            MySQLのフォーマットにしたがった日付をあらわす文字列
	 * @return カレンダーオブジェクト
	 * @version 1.1
	 * @since 1.1
	 */
	public static Calendar stringToDate(String str) {
		return MyDB.stringToDatetime(str + " 00:00:00");
	}

	/**
	 * MySQLのフォーマットに沿った時刻をあらわす文字列を、カレンダーオブジェクトに変換します。
	 * 日付は０年０月０日を指定します。
	 *
	 * @param str
	 *            MySQLのフォーマットにしたがった時刻をあらわす文字列
	 * @return カレンダーオブジェクト
	 * @version 1.1
	 * @since 1.1
	 */
	public static Calendar stringToTime(String str) {
		return MyDB.stringToDatetime("0000-00-00 " + str);
	}

	/**
	 * 結果オブジェクトに含まれる日時を表すカラムを、直接取り出してカレンダーオブジェクトに変換します。
	 * 
	 * @param rs
	 *            クエリを実行した結果オブジェクト
	 * @param columnName
	 *            カレンダーオブジェクトに変換するデータが格納されたカラム名
	 * @return カレンダーオブジェクト
	 * @throws SQLException
	 *             データベースとのやり取りに失敗した場合、この例外がスローされます。
	 * @version 1.1
	 * @since 1.1
	 */
	public static Calendar getCalendar(ResultSet rs, String columnName) throws SQLException {
		Date d = rs.getDate(columnName);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}

}
