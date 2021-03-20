package tk.iovr.mall_learning001;

import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MallLearning001Application {

	public static void main(String[] args) {
		String Str = "编程字典:www.CodingDict.com";
		System.out.println(Str.lastIndexOf( 'i', 11));
//		SpringApplication.run(MallLearning001Application.class, args);
	}

}
