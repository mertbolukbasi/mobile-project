from PIL import Image

def super_clean(image_path):
    img = Image.open(image_path).convert("RGBA")
    data = img.getdata()
    
    new_data = []
    for r, g, b, a in data:
        # If it's mostly white or light gray, wipe it
        # (even if it wasn't connected to the corners)
        brightness = (r + g + b) / 3
        # If it's very bright AND neutral-ish (gray/white)
        is_neutral = abs(r-g) < 15 and abs(g-b) < 15
        
        if (brightness > 220 and is_neutral) or (a < 50):
            new_data.append((0, 0, 0, 0))
        else:
            new_data.append((r, g, b, a))
            
    img.putdata(new_data)
    img.save(image_path, "PNG")

super_clean('/home/burak/Desktop/mobile-project/app/src/main/res/drawable/ic_paginex_icon.png')
print("Super clean pass done")
