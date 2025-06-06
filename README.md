
<img src="images/logo.png" width="150" />

# GroceryLens 

GroceryLens is an AI powered mobile application that aims to help users reduce 
food waste by turning ingredients they have into recipe ideas.
the application combines Machine learning components (Computer Vision and Natural Language Processing)
to achieve this goal.

---
## Features

### Food Detection

A YOLOv8 model was trained on a custom dataset of 17 food classes [Common Food Items Dataset (Kaggle)](https://www.kaggle.com/datasets/michaelorebela/common-food-items-dataset)
consisting of 'banana', 'beef', 'bread', 'broccoli', 'cheese', 'chicken', 'cucumber', 'egg', 'fish', 'lemon', 'lettuce', 'milk', 
'mushroom', 'onion', 'orange', 'potato', 'tomato'.
Ingredients are detected and then sent to customisation screen for recipe generation.
The model runs fully on-device for fast inference

<p float="left">
  <img src="images/camera.png" width="200" />
  <img src="images/selection.png" width="200" />
</p>







