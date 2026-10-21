"use client";

import { useRef } from "react";

export function ZoomImage({
  src,
  alt,
}: {
  src: string;
  alt: string;
}) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  return (
    <figure className="preview">
      <button type="button" className="zoom" onClick={() => dialogRef.current?.showModal()}>
        <img src={src} alt={alt} width={1600} height={1067} />
        <span>Enlarge</span>
      </button>
      <figcaption>Reference photograph. The exact unit is confirmed with the quote.</figcaption>
      <dialog
        ref={dialogRef}
        className="zoom-dialog"
        onClick={(event) => {
          if (event.target === dialogRef.current) dialogRef.current?.close();
        }}
      >
        <img src={src} alt={alt} />
        <button type="button" className="button" onClick={() => dialogRef.current?.close()}>
          Close
        </button>
      </dialog>
    </figure>
  );
}
