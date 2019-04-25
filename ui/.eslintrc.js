module.exports = {
  extends: [
    // add more generic rulesets here, such as:
    // 'eslint:recommended',
    'plugin:vue/recommended'
  ],
  rules: {
    // override/add rules settings here, such as:
    // 'vue/no-unused-vars': 'error'
    'no-console': 'off',
    "vue/component-name-in-template-casing": [ "error", 
      "kebab-case", { "ignores": [] } 
    ]
  }
}