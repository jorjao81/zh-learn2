Feature: Improve Anki Command
  As a CLI user
  I want to improve specific fields in existing Anki export files
  So that I can update audio, explanations, or examples without regenerating everything

  Scenario: Improve audio only for existing Anki export
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	学习	xué xí		to study; to learn	<div>我在<b>学习</b>中文。<br>Wǒ zài <b>xué xí</b> zhōng wén.<br>I am studying Chinese.</div>	This is a short explanation.	学(study) + 习(practice)	y		y
      """
    When I run improve-anki with parameters "--improve-audio --audio-selections=学习:qwen-tts:Cherry"
    Then the exit code should be 0
    And the improved Anki export file should exist
    And the audio field of "学习" should not be empty
    And the etymology field of "学习" should remain unchanged

  Scenario: Improve explanation only for existing Anki export
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	秽	huì4		(orig.) to be overrun with weeds → weeds ⇒ dirty, filthy => debauchery	<div>污<b>秽</b><br>wū <b>huì</b><br>filthy</div>	This is a short explanation.	禾(grain) + 歲(year)	y		y
      """
    When I run improve-anki with parameters "--improve-explanation --explanation-provider=openrouter --model=google/gemini-2.5-flash-lite-preview-09-2025"
    Then the exit code should be 0
    And the improved Anki export file should exist
    And the etymology field of "秽" should contain more than 500 characters
    And the examples field of "秽" should remain unchanged
    And the audio field of "秽" should remain unchanged

  Scenario: Improve explanation and examples for existing Anki export
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	液态	yè tài		noun liquid state; liquidness	<div>水是<b>液态</b>的。<br>Shuǐ shì <b>yè tài</b> de.<br>Water is liquid.</div>	Short explanation here.	液(liquid) + 态(state)	y		y
      Chinese 2	秽	huì4		(orig.) to be overrun with weeds → weeds ⇒ dirty, filthy => debauchery	<div>污<b>秽</b><br>wū <b>huì</b><br>filthy</div>	Another short one.	禾(grain) + 歲(year)	y		y
      """
    When I run improve-anki with parameters "--improve-explanation --improve-examples --explanation-provider=openrouter --example-provider=openrouter --model=google/gemini-2.5-flash-lite-preview-09-2025"
    Then the exit code should be 0
    And the improved Anki export file should exist
    And the etymology field of "液态" should contain more than 500 characters
    And the etymology field of "秽" should contain more than 500 characters
    And the examples field of "液态" should contain more than 200 characters
    And the examples field of "秽" should contain more than 200 characters
    And the audio field of "液态" should remain unchanged
    And the audio field of "秽" should remain unchanged

  @images
  Scenario: Improve without --improve-images flag
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	学习	xué xí		to study; to learn	<div>我在<b>学习</b>中文。<br>Wǒ zài <b>xué xí</b> zhōng wén.<br>I am studying Chinese.</div>	Brief note.	学(study) + 习(practice)	y		y
      """
    And the Anki media directory is set up
    When I run improve-anki without --improve-images flag
    Then the exit code should be 0
    And no images are downloaded
    And the definition field contains plain text only

  @images
  Scenario: Missing API key fails fast
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	灯塔	dēng tǎ		lighthouse	<div>远处的<b>灯塔</b>指引着船只。<br>Yuǎn chù de <b>dēng tǎ</b> zhǐyǐn zhe chuánzhī.<br>The distant lighthouse guides the ships.</div>	Brief explanation.	石(stone) + 灯(lamp)	y		y
      """
    And the Anki media directory is set up
    And Google Search API key is not configured
    When I run improve-anki with image parameters "--improve-images --image-selections=灯塔:1"
    Then the exit code should be non-zero
    And the error message should mention "GOOGLE_SEARCH_API_KEY"

  @images
  Scenario: No images found fails fast
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	罕见测试	hǎn jiàn cè shì		unique zxqvzqvnonexistentconcept token	<div>这是一条<b>罕见测试</b>语句。</div>	Brief explanation.	罕(rare) + 见(see)	y		y
      """
    And Google Custom Search API is configured
    And the Anki media directory is set up
    When I run improve-anki with image parameters "--improve-images --image-selections=罕见测试:1"
    Then the exit code should be non-zero
    And the error message should mention "no images found"

  @images
  Scenario: Anki media directory not found fails fast
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	大象	dà xiàng		elephant	<examples>	explanation	components	y		y
      """
    And Google Custom Search API is configured
    And Anki media directory does not exist
    When I run improve-anki with image parameters "--improve-images --image-selections=大象:1,2"
    Then the exit code should be non-zero
    And the error message should mention "Anki media directory"

  @images
  Scenario: Improve with images for existing Anki export
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	大象	dà xiàng		elephant	<examples>	explanation	components	y		y
      """
    And Google Custom Search API is configured
    And the Anki media directory is set up
    When I run improve-anki with image parameters "--improve-images --image-selections=大象:1,2,3"
    Then the exit code should be 0
    And the improved Anki export file should exist
    And the definition field of "大象" should contain image references
    And the Anki media directory should contain 3 images for "大象"

  Scenario: Improve Anki export with missing definitions using --improve-definition
    Given I have an Anki export file with content:
      """
      #separator:tab
      #html:true
      #notetype column:1
      Chinese 2	交通拥堵费	jiāo tōng yōng dǔ fèi			<div>example placeholder</div>	explanation here	components	y		y
      Chinese 2	学习	xué xí			<div>another example</div>	another explanation	学(study) + 习(practice)	y		y
      """
    When I run improve-anki with parameters "--improve-definition --definition-generator-provider=openrouter --definition-formatter-provider=openrouter --model=google/gemini-2.5-flash-lite-preview-09-2025"
    Then the exit code should be 0
    And the improved Anki export file should exist
    And the definition field of "交通拥堵费" should not be empty
    And the definition field of "交通拥堵费" should not contain "[No definition available"
    And the definition field of "学习" should not be empty
    And the definition field of "学习" should not contain "[No definition available"
    And the etymology field of "交通拥堵费" should remain unchanged
    And the examples field of "学习" should remain unchanged
