import os
import json
import numpy as np
import tensorflow as tf
from collections import Counter

# Configuration
DATASET_PATH = r"C:\Users\Admin\Desktop\eye_dataset"
TRAIN_DIR = os.path.join(DATASET_PATH, "train")
VAL_DIR   = os.path.join(DATASET_PATH, "val")
MODEL_DIR = "deep_learning_models"
os.makedirs(MODEL_DIR, exist_ok=True)

IMG_SIZE = (224, 224)
BATCH = 16
EPOCHS_HEAD = 10
EPOCHS_FINE = 20
SEED = 42

print("Loading datasets...")
train_ds = tf.keras.utils.image_dataset_from_directory(
    TRAIN_DIR,
    image_size=IMG_SIZE,
    batch_size=BATCH,
    seed=SEED,
    label_mode="int",
)

val_ds = tf.keras.utils.image_dataset_from_directory(
    VAL_DIR,
    image_size=IMG_SIZE,
    batch_size=BATCH,
    seed=SEED,
    label_mode="int",
)

class_names = train_ds.class_names
num_classes = len(class_names)
print("Classes:", class_names)

# Diagnostic: Count labels correctly
print("Counting labels for weights...")
y_train = []
for _, labels in train_ds:
    y_train.extend(labels.numpy().tolist())
counts = Counter(y_train)
total = sum(counts.values())
class_weight = {i: total / (num_classes * counts[i]) if counts[i] > 0 else 1.0 for i in range(num_classes)}
print("Train counts:", dict(sorted(counts.items())))
print("Class weights:", class_weight)

# Augmentation and Preprocessing
AUTOTUNE = tf.data.AUTOTUNE

# Simplified augmentation for stability first
augment_layers = tf.keras.Sequential([
    tf.keras.layers.RandomFlip("horizontal_and_vertical"),
    tf.keras.layers.RandomRotation(0.1),
    tf.keras.layers.RandomZoom(0.1),
])

def train_map(image, label):
    # Augment then Rescale is NOT needed for EfficientNetB0 as it has internal rescaling
    # But image_dataset_from_directory returns float tensors. B0 expects [0, 255].
    image = augment_layers(image, training=True)
    return image, label

train_ds = train_ds.shuffle(1000).map(train_map, num_parallel_calls=AUTOTUNE).prefetch(AUTOTUNE)
val_ds = val_ds.prefetch(AUTOTUNE)

# Build Deep Learning Model
print("Building Deep Learning Model (EfficientNetB0)...")
base = tf.keras.applications.EfficientNetB0(
    include_top=False,
    input_shape=(IMG_SIZE[0], IMG_SIZE[1], 3),
    weights="imagenet"
)
base.trainable = False

inputs = tf.keras.Input(shape=(IMG_SIZE[0], IMG_SIZE[1], 3))
x = base(inputs, training=False)
x = tf.keras.layers.GlobalAveragePooling2D()(x)
x = tf.keras.layers.Dropout(0.4)(x)
outputs = tf.keras.layers.Dense(num_classes, activation="softmax")(x)

model = tf.keras.Model(inputs, outputs)

model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-3),
    loss=tf.keras.losses.SparseCategoricalCrossentropy(),
    metrics=["accuracy"]
)

checkpoint_path = os.path.join(MODEL_DIR, "best_eye_model.h5")
callbacks = [
    tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1),
    tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1),
    tf.keras.callbacks.ModelCheckpoint(filepath=checkpoint_path, monitor='val_accuracy', save_best_only=True, mode='max', verbose=1)
]

print("\n== Phase 1: Train Head ==")
try:
    model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=EPOCHS_HEAD,
        class_weight=class_weight,
        callbacks=callbacks
    )
except Exception as e:
    print(f"FAILED Phase 1: {e}")
    import traceback; traceback.print_exc()
    exit(1)

print("\n== Phase 2: Fine Tuning ==")
base.trainable = True
# Freeze early layers
for layer in base.layers[:-20]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-5),
    loss=tf.keras.losses.SparseCategoricalCrossentropy(),
    metrics=["accuracy"]
)

try:
    model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=EPOCHS_FINE,
        class_weight=class_weight,
        callbacks=callbacks
    )
except Exception as e:
    print(f"FAILED Phase 2: {e}")
    import traceback; traceback.print_exc()
    exit(1)

# Final Save
print("\nSaving final model...")
model.save(os.path.join(MODEL_DIR, "eye_model.h5"))
with open(os.path.join(MODEL_DIR, "labels.json"), "w") as f:
    json.dump(class_names, f)

print("Converting to TFLite...")
try:
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    with open(os.path.join(MODEL_DIR, "eye_model.tflite"), "wb") as f:
        f.write(tflite_model)
    print("TFLite success.")
except Exception as e:
    print(f"TFLite failed: {e}")

print("Done! Model saved to", MODEL_DIR)
