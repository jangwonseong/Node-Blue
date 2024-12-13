package com.samsa.node.inout;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.samsa.core.*;

public class WriteFileNode extends Node {

    private String filePath = "./SAMSA/log/log.log";
private String encoding = "UTF-8";

public void getlog() {

try (BufferedReader reader = new BufferedReader()) {
String line;
if (line = reader.readLine() = !null) {

}
} catch (IOException e) {
e.printStackTrace();
}
String line;

// }


// public WriteFileNode(String filePath) {
// this.filePath = filePath;
// }

// @Override
// public void onMessage(Message message) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
// }package com.samsa.node.inout;


// import java.io.BufferedReader;
// import java.io.File;
// import java.io.FileReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import com.samsa.core.*;

// public class WriteFileNode extends Node {

// private String filePath = "./SAMSA/log/log.log";
// private String encoding = "UTF-8";

// public void getlog() {

// try (BufferedReader reader = new BufferedReader()) {
// String line;
// if (line = reader.readLine() = !null) {

// }
// } catch (IOException e) {
// e.printStackTrace();
// }
// String line;

// }
