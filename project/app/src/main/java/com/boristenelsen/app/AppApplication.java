package com.boristenelsen.app;

import com.boristenelsen.app.database.models.Table;
import com.boristenelsen.app.database.services.DumpReader;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class AppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {

		/*
		String statement = "SELECT aa,bb FROM table";

		Statement stmt =  CCJSqlParserUtil.parse(statement);
		if(stmt instanceof Select) {
			Select select = (Select) stmt;
			PlainSelect plainSelect = (PlainSelect)select.getSelectBody();
			List<SelectItem> selectItemList = plainSelect.getSelectItems();
			if(selectItemList.get(0) instanceof AllColumns){
				System.out.println();
			}else if(selectItemList.get(0) instanceof SelectExpressionItem){
				List<String> columnNames = selectItemList.stream().map(SelectItem::toString).collect(Collectors.toList());
				System.out.println();
			}

		}
		*/

		/*
		File file = ResourceUtils.getFile("classpath:dump.sql");
		System.out.println(file.getAbsolutePath());
		DumpReader dumpReader = new DumpReader(file);
		dumpReader.initDatabase();

		 */
	}
}
