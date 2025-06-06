
<img src="images/logo.png" width="150" />

# GroceryLens 

GroceryLens is an AI powered mobile application that aims to help users reduce 
food waste by turning ingredients they have into recipe ideas.
the application combines Machine learning components (Computer Vision and Natural Language Processing)
to achieve this goal.

---
## Features

### 1) Food Detection

A YOLOv8 model was trained on a custom dataset of 17 food classes [Common Food Items Dataset (Kaggle)](https://www.kaggle.com/datasets/michaelorebela/common-food-items-dataset)
consisting of:

['banana', 'beef', 'bread', 'broccoli', 'cheese', 'chicken', 'cucumber', 'egg', 'fish', 'lemon', 'lettuce', 'milk', 
'mushroom', 'onion', 'orange', 'potato', 'tomato'].

Ingredients are detected and then sent to customisation screen for recipe generation.
The model runs fully on-device for fast inference

<p float="left">
  <img src="images/camera.png" width="200" />
  <img src="images/selection.png" width="200" />
</p>

### NLP Voice Detection

GroceryLens allows users to input ingredients through **voice commands**.
To process the input, a **FoodBERT NER API** was built using:
[chambliss/distilbert-for-food-extraction](https://huggingface.co/chambliss/distilbert-for-food-extraction)
and then Hosted as a REST API on **Google Cloud Run**.
This processes free-form voice text and extracts structured ingredient names.

<img src="images/voiceinput.png" width="200" />









