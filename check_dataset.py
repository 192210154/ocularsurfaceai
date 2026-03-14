import os

DATASET_PATH = r"C:\Users\Admin\Desktop\eye_dataset"
for split in ["train", "val"]:
    print(f"\n--- {split.upper()} ---")
    split_dir = os.path.join(DATASET_PATH, split)
    if not os.path.exists(split_dir):
        print(f"Directory not found: {split_dir}")
        continue
    
    for class_name in os.listdir(split_dir):
        class_path = os.path.join(split_dir, class_name)
        if os.path.isdir(class_path):
            files = os.listdir(class_path)
            img_files = [f for f in files if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
            print(f"{class_name}: {len(img_files)} images")
