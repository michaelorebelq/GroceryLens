
<img src="images/logo.png" width="150" />

# GroceryLens 

GroceryLens is an AI powered mobile application that aims to help users reduce 
food waste by turning ingredients they have into recipe ideas.
the application combines Machine learning components (Computer Vision and Natural Language Processing)
to achieve this goal.

---
## Features

### 1) Food Image Detection

A YOLOv8 model was trained on a custom dataset of 17 food classes [Common Food Items Dataset (Kaggle)](https://www.kaggle.com/datasets/michaelorebela/common-food-items-dataset)
consisting of:

['banana', 'beef', 'bread', 'broccoli', 'cheese', 'chicken', 'cucumber', 'egg', 'fish', 'lemon', 'lettuce', 'milk', 
'mushroom', 'onion', 'orange', 'potato', 'tomato'].

Ingredients are detected and then sent to customisation screen for recipe generation.
The model runs fully on-device for fast inference

<p float="left">
  <img src="images/camera.png" width="150" />
  <img src="images/selection.png" width="150" />
</p>

### 2) NLP Voice Detection

GroceryLens allows users to input ingredients through **voice commands**.
To process the input, a **FoodBERT NER API** was built using:

[chambliss/distilbert-for-food-extraction](https://huggingface.co/chambliss/distilbert-for-food-extraction)
and then Hosted as a REST API on **Google Cloud Run**.

This processes free-form voice text into extracted ingredient names.

For example:
> **Voice input:** *"I have an egg, three grams of flour, and a bottle of milk."*  
> **Extracted ingredients:** `[egg, flour, milk]`

Note on the first use, the API may experience a **cold start delay**.

<img src="images/voiceinput.png" width="150" />

### 3) Gamification 

GroceryLens also includes a gamified experience to encourage continued application usage.
Users progress through levels based on the number of recipes viewed.

A Circular progress indicator and badge system can be seen on the landing page,
and user progress is stored in Firebase Realtime Database and synced across devices.

<img src="images/landingpage.png" width="150" />








