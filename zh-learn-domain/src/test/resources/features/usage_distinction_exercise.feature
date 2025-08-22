Feature: Chinese Usage Distinction Fill-in-the-Blank Exercise
  As a Chinese language learning system
  I want to provide usage distinction exercises with multiple correct answers
  So that learners can develop nuanced understanding of word usage patterns

  Scenario: Exercise validation - no blanks
    Given the exercise:
      | sentence | 我去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | false   | Not appropriate for this context |
      | 计划   | false   | Does not fit the meaning |
    When I attempt to create a usage distinction exercise
    Then the exercise creation should fail
    And the error should indicate "Exercise must contain exactly one blank space"

  Scenario: Exercise validation - multiple blanks
    Given the exercise:
      | sentence | 我___明天___去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | false   | Not appropriate for this context |
      | 计划   | false   | Does not fit the meaning |
    When I attempt to create a usage distinction exercise
    Then the exercise creation should fail
    And the error should indicate "Exercise must contain exactly one blank space"

  Scenario: Exercise validation - insufficient answer options
    Given the exercise:
      | sentence | 我___明天去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | true    | Expresses intention/plan |
    When I attempt to create a usage distinction exercise
    Then the exercise creation should fail
    And the error should indicate "Exercise must have at least 2 options"

  Scenario: Exercise validation - no correct answers
    Given the exercise:
      | sentence | 我___明天去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | false   | Not appropriate for this context |
      | 计划   | false   | Does not fit the meaning |
    When I attempt to create a usage distinction exercise
    Then the exercise creation should fail
    And the error should indicate "Exercise must have at least one correct answer option"

  Scenario: Missing one correct answer
    Given the exercise:
      | sentence | 我___明天去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | true    | Expresses intention/plan, appropriate for concrete travel arrangements |
      | 计划   | true    | Indicates planning, fits well with business travel context |
      | 想要   | true    | Shows desire/intention, suitable for expressing travel plans |
      | 希望   | false   | Expresses hope/wish, not concrete planning for definite arrangements |
    When a learner selects "打算", "想要"
    Then the result should be incorrect

  Scenario: Selected incorrect option
    Given the exercise:
      | sentence | 这道菜___好吃。 |
    And answer options:
      | option | correct | explanation |
      | 很     | true    | Standard degree adverb, appropriately intensifies positive adjectives |
      | 非常   | true    | Strong intensifier, emphasizes high degree of deliciousness |
      | 特别   | true    | Emphasizes exceptional quality, fits well with taste evaluation |
      | 比较   | false   | Indicates moderate degree, contradicts strong positive evaluation |
    When a learner selects "很", "非常", "特别", "比较"
    Then the result should be incorrect

  Scenario: All correct answers selected
    Given the exercise:
      | sentence | 她正在___中文语法。 |
    And answer options:
      | option | correct | explanation |
      | 学习   | true    | Standard verb for studying, appropriate for grammar acquisition |
      | 学     | true    | Casual form of learning, fits well with progressive aspect |
      | 研究   | true    | Indicates deep study/research, suitable for grammar analysis |
      | 教     | false   | Means 'to teach', indicates knowledge transmission not acquisition |
    When a learner selects "学习", "学", "研究"
    Then the result should be correct

  Scenario: No answers selected
    Given the exercise:
      | sentence | 我___明天去北京出差。 |
    And answer options:
      | option | correct | explanation |
      | 打算   | true    | Expresses intention/plan, appropriate for concrete travel arrangements |
      | 计划   | true    | Indicates planning, fits well with business travel context |
      | 想要   | true    | Shows desire/intention, suitable for expressing travel plans |
      | 希望   | false   | Expresses hope/wish, not concrete planning for definite arrangements |
    When a learner selects no answers
    Then the result should be incorrect

  Scenario: Single correct answer exercise
    Given the exercise:
      | sentence | 她在___中文。 |
    And answer options:
      | option | correct | explanation |
      | 学     | true    | Appropriate verb for learning language |
      | 教     | false   | Means teaching, not learning |
    When a learner selects "学"
    Then the result should be correct

  Scenario: Maximum answer options
    Given the exercise:
      | sentence | 这个问题很___。 |
    And answer options:
      | option | correct | explanation |
      | 难     | true    | Standard word for difficult |
      | 困难   | true    | Formal term for difficult |
      | 复杂   | true    | Means complex/complicated |
      | 简单   | false   | Means simple, opposite meaning |
      | 容易   | false   | Means easy, opposite meaning |
      | 轻松   | false   | Means relaxed/easy, opposite meaning |
    When a learner selects "难", "困难", "复杂"
    Then the result should be correct

  Scenario: All options are correct
    Given the exercise:
      | sentence | 我___去看电影。 |
    And answer options:
      | option | correct | explanation |
      | 想     | true    | Want to go watch a movie |
      | 要     | true    | Going to watch a movie |
    When a learner selects "想", "要"
    Then the result should be correct

