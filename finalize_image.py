from PIL import Image

def finalize(image_path):
    img = Image.open(image_path).convert("RGBA")
    # Resize for better mobile performance and anti-aliasing
    base_width = 1024
    w_percent = (base_width / float(img.size[0]))
    h_size = int((float(img.size[1]) * float(w_percent)))
    img = img.resize((base_width, h_size), Image.Resampling.LANCZOS)
    
    img.save(image_path, "PNG")

finalize('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_splash_logo.png')
print("Finalize: Resized and saved")
