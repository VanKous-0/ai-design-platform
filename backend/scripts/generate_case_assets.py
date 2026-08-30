from pathlib import Path
from shutil import copy2

from PIL import Image, ImageOps


PROJECT_ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = (
    PROJECT_ROOT
    / "assets"
    / "项目成果统计表以及成果相关支撑材料"
    / "成果相关支撑材料"
)
OUTPUT_ROOT = (
    PROJECT_ROOT
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "static"
    / "assets"
    / "cases"
)


def save_board(source_name: str, relative_output: str) -> None:
    source = SOURCE_ROOT / source_name
    output = OUTPUT_ROOT / relative_output
    output.parent.mkdir(parents=True, exist_ok=True)

    with Image.open(source) as image:
        image = ImageOps.exif_transpose(image).convert("RGB")
        image.thumbnail((2400, 2400), Image.Resampling.LANCZOS)
        image.save(output, "JPEG", quality=84, optimize=True, progressive=True)


def save_cover(
    source_name: str,
    relative_output: str,
    vertical_focus: float,
) -> None:
    source = SOURCE_ROOT / source_name
    output = OUTPUT_ROOT / relative_output
    output.parent.mkdir(parents=True, exist_ok=True)

    with Image.open(source) as image:
        image = ImageOps.exif_transpose(image).convert("RGB")
        cover = ImageOps.fit(
            image,
            (1600, 900),
            method=Image.Resampling.LANCZOS,
            centering=(0.5, vertical_focus),
        )
        cover.save(output, "JPEG", quality=86, optimize=True, progressive=True)


def copy_document(source_name: str, relative_output: str) -> None:
    output = OUTPUT_ROOT / relative_output
    output.parent.mkdir(parents=True, exist_ok=True)
    copy2(SOURCE_ROOT / source_name, output)


def main() -> None:
    save_cover("游客中心展板(1).jpg", "visitor-center/cover.jpg", 0.1)
    save_board("游客中心展板(1).jpg", "visitor-center/board.jpg")
    copy_document("游客中心生成过程.pdf", "visitor-center/process.pdf")

    save_cover("未来设计师展板.jpg", "energy-station/cover.jpg", 0.1)
    save_board("未来设计师展板.jpg", "energy-station/board.jpg")
    copy_document("车站展板生成过程.pdf", "energy-station/process.pdf")

    save_cover("社区服务中心汇总.jpg", "neighborhood-center/cover.jpg", 0.1)
    save_board("社区服务中心汇总.jpg", "neighborhood-center/board-overview.jpg")
    save_board("展板一.png", "neighborhood-center/board-detail.jpg")

    save_cover("微花园展板.jpg", "boundless-garden/cover.jpg", 0.8)
    save_board("微花园展板.jpg", "boundless-garden/board.jpg")
    save_board("微花园获奖证书.jpg", "boundless-garden/award.jpg")


if __name__ == "__main__":
    main()
