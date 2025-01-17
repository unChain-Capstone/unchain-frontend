# -*- coding: utf-8 -*-
"""_c242_ps395_recommendation_system.ipynb

Automatically generated by Colab.

Original file is located at
    https://colab.research.google.com/github/aimldlnlp/C242-PS395/blob/main/_c242_ps395_recommendation_system.ipynb
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.metrics.pairwise import cosine_similarity
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, Embedding, Flatten, Concatenate, Dense, Dropout
from tensorflow.keras.optimizers import Adam
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import LabelEncoder
from tensorflow.keras.utils import to_categorical

from google.colab import drive

drive.mount('/content/drive', force_remount=True)

import pandas as pd

daily_logs = pd.read_csv('/content/drive/MyDrive/C242-PS395/data_users.csv')
food_data = pd.read_csv('/content/drive/MyDrive/C242-PS395/food_data.csv')

(daily_logs.head())

(food_data.head())

# Preprocess daily and weekly logs
daily_logs['Date'] = pd.to_datetime(daily_logs['Date'])

#preprocess daily logs
def preprocess_logs_day(daily_logs, daily_sugar_limit=50):
    # Aggregate daily sugar intake
    daily_agg = daily_logs.groupby(['User ID', 'Date']).agg({
        'Dish Name': ', '.join, 'Sugar': 'sum'}).reset_index()
    daily_agg.rename(columns={'User ID': 'user_id', 'Sugar': 'daily_sugar_intake', 'Dish Name': 'input_dish'}, inplace=True)

    # Classify sugar levels
    def classify_sugar(sugar):
        if sugar < 0.8 * daily_sugar_limit:
            return 'Low'
        elif sugar <= daily_sugar_limit:
            return 'Normal'
        else:
            return 'High'
    daily_agg['sugar_level'] = daily_agg['daily_sugar_intake'].apply(classify_sugar)

    return daily_agg

daily_agg = preprocess_logs_day(daily_logs)
(daily_agg.head())

#preprocess weekly logs
def preprocess_logs_week(daily_agg, daily_sugar_limit=50):

    # Aggregate weekly sugar intake
    daily_agg['Week'] = daily_agg['Date'].dt.to_period('W').apply(lambda r: r.start_time)
    weekly_agg = daily_agg.groupby(['user_id', 'Week']).agg({
        'input_dish': ', '.join, 'daily_sugar_intake': 'sum'}).reset_index()
    weekly_agg.rename(columns={'daily_sugar_intake': 'weekly_sugar_intake'}, inplace=True)

    # Classify sugar levels
    def classify_sugar(sugar):
        if sugar < 0.8 * daily_sugar_limit * 7:
            return 'Low'
        elif sugar <= daily_sugar_limit * 7:
            return 'Normal'
        else:
            return 'High'
    weekly_agg['sugar_level'] = weekly_agg['weekly_sugar_intake'].apply(classify_sugar)

    return weekly_agg

weekly_agg = preprocess_logs_week(daily_agg)
(weekly_agg.head())

X = weekly_agg[['weekly_sugar_intake']]  # features
y = weekly_agg['sugar_level']            # targets

from sklearn.preprocessing import LabelEncoder
label_encoder = LabelEncoder()
y_encoded = label_encoder.fit_transform(y)  # 'Low' -> 0, 'Normal' -> 1, 'High' -> 2

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score

X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.2, random_state=42)

model = RandomForestClassifier(random_state=42)
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print(classification_report(y_test, y_pred))
print("Accuracy:", accuracy_score(y_test, y_pred))

"""SYSTEM RECOMMENDATION"""

import tensorflow as tf
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.model_selection import train_test_split

# TF-IDF Vectorizer for the food list
tfidf_vectorizer = TfidfVectorizer()
food_texts = food_data['food'].tolist()  # List of food names
user_foods = weekly_agg['input_dish'].tolist()  # List of food consumed by users
food_tfidf = tfidf_vectorizer.fit_transform(food_texts)
user_food_tfidf = tfidf_vectorizer.transform(user_foods)

# Prepare input features: combine food vectors and sugar intake
user_sugar = weekly_agg['weekly_sugar_intake'].values.reshape(-1, 1)
user_features = np.hstack([user_food_tfidf.toarray(), user_sugar])

# Encode sugar levels to numeric (for supervised learning)
from sklearn.preprocessing import LabelEncoder
label_encoder = LabelEncoder()
weekly_agg['sugar_level_encoded'] = label_encoder.fit_transform(weekly_agg['sugar_level'])

# Prepare target (encoded sugar levels)
y = weekly_agg['sugar_level_encoded'].values

# Split data into training and test sets
X_train, X_test, y_train, y_test = train_test_split(user_features, y, test_size=0.2, random_state=42)

# Build TensorFlow model for recommendation
model = tf.keras.Sequential([
    tf.keras.layers.InputLayer(input_shape=(X_train.shape[1],)),  # Input layer (food vectors + sugar intake)
    tf.keras.layers.Dense(128, activation='relu'),  # Hidden layer 1
    tf.keras.layers.Dense(64, activation='relu'),   # Hidden layer 2
    tf.keras.layers.Dense(3, activation='softmax')  # Output layer (sugar levels)
])

# Compile the model
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# Train the model
history = model.fit(X_train, y_train, epochs=50, batch_size=2, validation_data=(X_test, y_test))

import matplotlib.pyplot as plt

plt.plot(history.history['accuracy'], label='Training Accuracy')
plt.plot(history.history['val_accuracy'], label='Validation Accuracy')
plt.title('Training and Validation Accuracy')
plt.xlabel('Epochs')
plt.ylabel('Accuracy')
plt.legend()
plt.show()

plt.plot(history.history['loss'], label='Train Loss')
plt.plot(history.history['val_loss'], label='Validation Loss')
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.legend()
plt.show()

# Make predictions for user recommendations
def recommend_food(user_id, weekly_agg, food_data, model, top_n=5):
    user_info = weekly_agg[weekly_agg['user_id'] == user_id].iloc[0]

    # Get the food list for the user
    user_food_list = user_info['input_dish']
    user_food_tfidf = tfidf_vectorizer.transform([user_food_list]).toarray()

    # Get the user's sugar intake
    user_sugar_intake = user_info['weekly_sugar_intake']

    # Combine the food vectors and sugar intake
    user_input = np.hstack([user_food_tfidf, np.array([[user_sugar_intake]])])

    # Predict sugar level for the user
    user_pred = model.predict(user_input)
    user_pred_class = np.argmax(user_pred, axis=1)[0]

    # Print predicted sugar level for the user
    predicted_sugar_level = label_encoder.inverse_transform([user_pred_class])[0]
    print(f"Predicted sugar level for user {user_id}: {predicted_sugar_level}")

    # Compute cosine similarity between user food list and all food items
    cosine_sim = cosine_similarity(user_food_tfidf, food_tfidf).flatten()

    # Get the top N most similar food items
    recommended_foods = food_data.iloc[np.argsort(cosine_sim)[-top_n:]]

    return recommended_foods[['food', 'Sugars']]

# Example: Recommend foods for user
recommended_foods = recommend_food(11, weekly_agg, food_data, model, top_n=5)
print("Recommended foods based on similarity and sugar level:")
print(recommended_foods)

import tensorflow as tf

# Path to save the converted TFLite model
tflite_model_path = 'recommendation_system.tflite'

# Convert the model to TFLite format
converter = tf.lite.TFLiteConverter.from_keras_model(model)  # Pass the trained model
tflite_model = converter.convert()

# Save the TFLite model
with open(tflite_model_path, 'wb') as f:
    f.write(tflite_model)

print(f"Model successfully converted and saved at {tflite_model_path}")

# Load the TFLite model
interpreter = tf.lite.Interpreter(model_path='recommendation_system.tflite')
interpreter.allocate_tensors()

# Get input and output details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Display the input and output details
print("Input Details:", input_details)
print("Output Details:", output_details)

# Prepare test input
import numpy as np
test_input = np.random.rand(1, 1376).astype(np.float32)  # Replace with real test data

# Perform inference
interpreter.set_tensor(input_details[0]['index'], test_input)
interpreter.invoke()

# Get and print the output
output = interpreter.get_tensor(output_details[0]['index'])
print("Model Output:", output)