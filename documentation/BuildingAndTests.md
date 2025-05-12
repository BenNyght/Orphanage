# Building

1. **Prerequisites**  
   - [Node.js](https://nodejs.org/)  
   - [npm](https://www.npmjs.com/)  
   - [VS Code](https://code.visualstudio.com/)

2. **Install dependencies**  

 ```bash
 npm install
 ```

3. **Compile**

 ```bash
 npm run compile
 ```

4. **(Optional) Package as VSIX**

 ```bash
 npm install -g vsce
 vsce package
 ```

# Running Tests

1. **Compile & test together:**

 ```bash
 npm test
 ```

- This uses the default Mocha-based test runner with ```@vscode/test-electron```.

2. **View** results in the terminal, or open the Test Explorer in VS Code (depending on your setup).

3. **Integration**
 - If you have special test configs, you can press <kbd>F5</kbd> and select “Extension Tests” in the debug dropdown to run them in a dedicated Extension Development Host.