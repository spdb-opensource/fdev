module.exports = {
  root: true,
  env: {
    node: true
  },
  'extends': [
    'plugin:vue/essential',
    'prettier'
  ],
  plugins: ['prettier'],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-unsafe-finally': 'error',
    'no-unused-vars': ["error", { "args": "none" }],
    'vue/no-unused-components':['error'],
    'prettier/prettier': [
      'error',
      {
        singleQuote: true
      }
    ]
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
}
