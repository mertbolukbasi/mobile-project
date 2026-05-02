from PIL import Image

def analyze(image_path):
    img = Image.open(image_path).convert("RGBA")
    w, h = img.size
    pixels = img.load()
    print("Corners:", pixels[0,0], pixels[w-1,0], pixels[0,h-1], pixels[w-1,h-1])

analyze("app/src/main/res/drawable/ic_paginex_icon.png")
